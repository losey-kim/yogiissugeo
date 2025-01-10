package com.yogiissugeo.android.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.opencsv.CSVReader
import com.yogiissugeo.android.BuildConfig
import com.yogiissugeo.android.data.api.ClothingBinApiHandler
import com.yogiissugeo.android.data.api.GeocodingApi
import com.yogiissugeo.android.data.local.dao.BookmarkDao
import com.yogiissugeo.android.data.local.dao.ClothingBinDao
import com.yogiissugeo.android.data.local.dao.DistrictDataCountDao
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.BookmarkType
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.data.model.GeocodingResponse
import com.yogiissugeo.android.utils.common.AddressCorrector
import com.yogiissugeo.android.utils.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.math.ceil

/**
 * 의류 수거함 데이터를 관리하는 Repository 클래스
 *
 * @property apiHandler 의류 수거함 관련 API 호출을 처리하는 핸들러 객체
 * @property geocodingApi 주소를 좌표로 변환하는 Geocoding API 호출을 처리하는 객체
 * @property clothingBinDao 의류 수거함 데이터를 저장하고 불러오는 DAO 객체
 * @property districtDataCountDao 특정 구의 데이터 카운트를 관리하는 DAO 객체
 * @property bookmarkDao 저장한 수거함 정보를 관리하는 DAO 객체
 */
class ClothingBinRepository @Inject constructor(
    private val apiHandler: ClothingBinApiHandler,
    private val geocodingApi: GeocodingApi,
    private val clothingBinDao: ClothingBinDao,
    private val districtDataCountDao: DistrictDataCountDao,
    private val bookmarkDao: BookmarkDao,
    @ApplicationContext private val context: Context
) {
    // 총 개수를 캐싱하기 위한 변수
    private var cachedTotalCount: MutableMap<String, Int> = mutableMapOf()
    // 전체 페이지 수를 캐싱하기 위한 변수
    private var cachedTotalPage: MutableMap<String, Int> = mutableMapOf()

    /**
     * 특정 구의 데이터를 가져옴 (페이징 처리)
     *
     * @param apiSource API 소스 정보
     * @param page 요청한 페이지 번호 (0부터 시작)
     * @param perPage 페이지당 데이터 개수 (기본값 100)
     * @return 요청된 페이지에 대한 의류 수거함 리스트
     */
    suspend fun getOrFetchBins(
        apiSource: ApiSource,
        page: Int,
        perPage: Int = 100
    ): Result<List<ClothingBin>> {
        // 1. DB에 이미 데이터가 있는지 확인
        val sourceName = apiSource.name
        val totalCount = cachedTotalCount[sourceName] ?: getTotalCount(apiSource).also {
            cachedTotalCount[sourceName] = it
        }

        // 2. 페이징 범위 확인
        if (isPageOutOfBounds(page, perPage, totalCount)) {
            // 요청한 페이지가 데이터 범위를 초과한 경우 빈 리스트 반환
            return Result.success(emptyList())
        }

        // 3. Room에서 데이터 가져오기
        val offset = (page - 1) * perPage
        val binsInDb = clothingBinDao.getBinsByDistrict(apiSource.name, perPage, offset)
        val storedCount = clothingBinDao.getStoredCountForDistrict(apiSource.name)

        // 4. 캐싱된 데이터가 충분한 경우 바로 반환
        return if (isDataSufficient(offset, storedCount)) {
            Result.success(binsInDb)
        } else {
            // 5. DB에 없다면, CSV or API 호출
            fetchBins(apiSource, page, perPage)
        }
    }

    /**
     * apiSource 타입에 따라 CSV or API 호출하는 함수
     */
    private suspend fun fetchBins(apiSource: ApiSource, page: Int, perPage: Int): Result<List<ClothingBin>> {
        return if (apiSource.isCsvSource) {
            loadBinsFromCsv(apiSource, perPage)
        } else {
            fetchAndStoreBinsFromApi(apiSource, page, perPage)
        }
    }

    /**
     * CSV에서 읽고 DB 저장
     */
    private suspend fun loadBinsFromCsv(apiSource: ApiSource, perPage: Int): Result<List<ClothingBin>> = withContext(Dispatchers.IO) {
        try {
            val csvFileName = apiSource.csvName
                ?: return@withContext Result.failure(IllegalStateException("CSV 파일을 찾을 수 없음."))

            // 1. CSV 파일 열기
            context.assets.open(csvFileName).use { inputStream ->
                // 2. CSV 파싱
                val bins = parseCsv(inputStream, apiSource)

                // 3. DB 저장
                saveBinsToDatabase(apiSource.name, bins, bins.size)

                // 4. 처음 저장 후 perPage 크기만큼만 보여줌
                Result.success(bins.take(perPage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * API를 호출하여 데이터를 가져오고 Room에 저장
     *
     * @param apiSource API 소스 정보
     * @param page 요청한 페이지 번호
     * @param perPage 페이지당 데이터 개수
     * @return API 호출 결과로 가져온 의류 수거함 리스트
     */
    private suspend fun fetchAndStoreBinsFromApi(
        apiSource: ApiSource,
        page: Int,
        perPage: Int,
    ): Result<List<ClothingBin>> {
        return safeApiCall {
            // Remote Config에서 API 키 가져오기
            val apiKey = BuildConfig.CLOTHING_BIN_API_KEY

            // API로부터 데이터 가져오기
            val response = apiHandler.fetchClothingBins(apiSource, page, perPage, apiKey)
            val body = response.body()
            val formattedData = body?.formattedData.orEmpty()

            // 1. 위도와 경도가 없는 데이터를 필터링 및 처리
            val binsWithCoordinates = processMissingCoordinates(formattedData)

            // 2. 기존 데이터와 업데이트된 데이터를 병합
            val allBins = mergeBins(formattedData, binsWithCoordinates)

            // 3. Room에 데이터 저장 및 총 데이터 카운트 업데이트
            saveBinsToDatabase(apiSource.name, allBins, body?.totalCount ?: 0)

            // 4. 응답 생성 및 반환
            Response.success(allBins, response.raw())
        }
    }

    /**
     * 북마크된 수거함 데이터를 페이징 형태로 가져옴.
     * 한 페이지당 20개의 아이템을 로드
     * Flow 형태로 데이터를 제공.
     */
    fun getBookmarkBinsPaged(district: String? = null): Flow<PagingData<ClothingBin>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20, // 한 페이지당 아이템 수
                enablePlaceholders = false
            ),
            pagingSourceFactory = { clothingBinDao.getBookmarkBins(district) }
        ).flow
    }

    /**
     * 북마크된 수거함 갯수를 가져옴
     */
    fun getBookmarkBinsCount(district: String? = null): Flow<Int> {
        return clothingBinDao.getBookmarkBinsCount(district)
    }

    /**
     * 북마크된 수거함 데이터를 페이징 형태로 가져옴.
     * 저장된 수거함 데이터 전체를 가져옴.
     */
    fun getAllBookmarkedBins(): Flow<List<ClothingBin>> {
        return clothingBinDao.getAllBookmarkedBins()
    }

    /**
     * 저장 상태를 toggle합니다.
     *
     * @param binId toggle할 binId
     */
    suspend fun toggleBookmark(binId: String): BookmarkType {
        val isBookmark = bookmarkDao.toggleBookmark(binId)
        clothingBinDao.updateBookmarkStatus(binId, !isBookmark)
        return if (isBookmark){ // 저장, 삭제 여부에 따라 BookmarkType return
            BookmarkType.REMOVE_SUCCESS
        } else {
            BookmarkType.ADD_SUCCESS
        }
    }

    /**
     * 요청한 페이지가 데이터 범위를 초과했는지 확인
     *
     * @param page 요청한 페이지 번호
     * @param perPage 페이지당 데이터 개수
     * @param totalCount 전체 데이터 개수
     * @return 페이지가 유효하지 않을 경우 true
     */
    private fun isPageOutOfBounds(page: Int, perPage: Int, totalCount: Int): Boolean {
        return (page - 1) * perPage >= totalCount && totalCount > 0
    }

    /**
     * 캐싱된 데이터가 요청한 페이지 범위에 충분한지 확인
     *
     * @param offset 데이터 시작 위치
     * @param storedCount 저장된 데이터 개수
     * @return 데이터가 충분할 경우 true
     */
    private fun isDataSufficient(offset: Int, storedCount: Int): Boolean {
        return offset < storedCount
    }

    /**
     * 위도와 경도가 없는 데이터를 처리 (주소 정제 및 좌표 변환)
     *
     * @param data API에서 가져온 의류 수거함 데이터 리스트
     * @return 좌표가 업데이트된 의류 수거함 데이터 리스트
     */
    private suspend fun processMissingCoordinates(data: List<ClothingBin>): List<ClothingBin> {
        return data.filter { it.latitude == null || it.longitude == null }
            .mapNotNull { bin ->
                // 주소 정제
                val correctedAddress = bin.address?.let(AddressCorrector::correct)
                // 좌표 요청 및 결과 처리
                correctedAddress?.let { address ->
                    getCoordinates(address).fold(
                        onSuccess = { geoData ->
                            geoData.addresses.firstOrNull()?.let { coordinates ->
                                // 위도/경도를 업데이트한 새 객체 반환
                                bin.copy(
                                    address = address,
                                    latitude = coordinates.y,
                                    longitude = coordinates.x
                                )
                            }
                        },
                        onFailure = { null } // 실패 시 null
                    )
                }
            }
    }

    /**
     * 데이터를 Room에 저장 및 카운트 업데이트
     *
     * @param districtName 구 이름
     * @param bins 의류 수거함 데이터 리스트
     * @param totalCount API에서 반환된 전체 데이터 개수
     */
    private suspend fun saveBinsToDatabase(
        districtName: String,
        bins: List<ClothingBin>,
        totalCount: Int
    ) {
        // 데이터 삽입
        clothingBinDao.insertBins(bins)
        // 총 데이터 카운트 삽입 또는 업데이트
        districtDataCountDao.insertOrUpdateCount(districtName, totalCount)

        // 데이터가 변경될 때 캐시 무효화
        invalidateCache(districtName)
    }

    /**
     * 주소를 사용하여 해당 위치의 좌표(위도, 경도)를 가져옵니다.
     *
     * @param address 변환할 주소 문자열
     * @return GeocodingResponse 객체를 포함하는 Result 객체
     *
     * - 내부적으로 safeApiCall을 사용하여 API 요청 중 발생할 수 있는 예외를 처리합니다.
     */
    private suspend fun getCoordinates(address: String): Result<GeocodingResponse> {
        // Remote Config에서 API 키 가져오기
        val naverApiKey = BuildConfig.NAVER_MAP_API_KEY
        val naverClientId = BuildConfig.NAVER_MAP_CLIENT_ID

        return safeApiCall {
            geocodingApi.getCoordinates(
                address,
                naverClientId,
                naverApiKey
            )
        }
    }

    /**
     * 기존 데이터 리스트와 업데이트된 데이터를 병합하여 최종 리스트를 생성합니다.
     *
     * @param originalData 기존 의류 수거함 데이터 리스트.
     * @param updatedData 좌표 정보가 추가된 업데이트된 의류 수거함 데이터 리스트.
     * @return 병합된 의류 수거함 데이터 리스트.
     *         동일한 ID를 가진 항목은 updatedData의 데이터를 우선적으로 사용하며,
     *         업데이트된 데이터가 없는 항목은 originalData를 유지합니다.
     */
    private fun mergeBins(
        originalData: List<ClothingBin>,
        updatedData: List<ClothingBin>
    ): List<ClothingBin> {
        // updatedData를 ID를 키로 하는 Map으로 변환
        val updatedMap = updatedData.associateBy { it.id }

        // originalData를 순회하면서 updatedMap에 있는 경우 업데이트된 데이터를 사용
        return originalData.map { bin -> updatedMap[bin.id] ?: bin }
    }

    //전체 페이지 수를 가져옴
    private suspend fun getTotalCount(apiSource: ApiSource): Int {
        return districtDataCountDao.getTotalCount(apiSource.name)?: 0
    }

    // 전체 페이지 수를 가져옴 (캐싱 포함)
    suspend fun getTotalPage(apiSource: ApiSource, perPage: Int): Int {
        val sourceName = apiSource.name
        return cachedTotalPage.getOrPut(sourceName) {
            ceil(getTotalCount(apiSource).toDouble() / perPage).toInt()
        }
    }

    // 데이터가 변경될 때 캐시 무효화
    private fun invalidateCache(apiSourceName: String) {
        cachedTotalCount.remove(apiSourceName)
        cachedTotalPage.remove(apiSourceName)
    }

    // CSV 데이터 파싱 함수 추가
    private fun parseCsv(inputStream: InputStream, apiSource: ApiSource): List<ClothingBin> {
        val reader = CSVReader(InputStreamReader(inputStream))
        val bins = mutableListOf<ClothingBin>()

        // ApiSource의 Companion Object에서 파서 가져오기
        val parser = ApiSource.csvParsers[apiSource] ?: return emptyList()

        reader.readNext() // 헤더 건너뛰기
        reader.forEachIndexed { index, row ->
            parser(index, row)?.let { bins.add(it) }
        }

        return bins
    }
}