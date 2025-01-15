package com.yogiissugeo.android.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.yogiissugeo.android.R
import com.yogiissugeo.android.data.api.ClothingBinApiHandler
import com.yogiissugeo.android.data.api.GeocodingApi
import com.yogiissugeo.android.data.local.dao.BookmarkDao
import com.yogiissugeo.android.data.local.dao.ClothingBinDao
import com.yogiissugeo.android.data.local.dao.DistrictDataCountDao
import com.yogiissugeo.android.data.model.Address
import com.yogiissugeo.android.data.model.ApiSource
import com.yogiissugeo.android.data.model.BookmarkType
import com.yogiissugeo.android.data.model.ClothingBin
import com.yogiissugeo.android.data.model.ClothingBinResponse
import com.yogiissugeo.android.data.model.GeocodingResponse
import com.yogiissugeo.android.utils.network.ResourceException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

@RunWith(MockitoJUnitRunner::class)
class ClothingBinRepositoryTest {
    @Mock
    private lateinit var apiHandler: ClothingBinApiHandler

    @Mock
    private lateinit var geocodingApi: GeocodingApi

    @Mock
    private lateinit var clothingBinDao: ClothingBinDao

    @Mock
    private lateinit var districtDataCountDao: DistrictDataCountDao

    @Mock
    private lateinit var bookmarkDao: BookmarkDao

    @Mock
    private lateinit var context: Context

    @Mock
    lateinit var assetManager: AssetManager

    private lateinit var repository: ClothingBinRepository

    @Before
    fun setUp() {
        repository = ClothingBinRepository(
            apiHandler = apiHandler,
            geocodingApi = geocodingApi,
            clothingBinDao = clothingBinDao,
            districtDataCountDao = districtDataCountDao,
            bookmarkDao = bookmarkDao,
            context = context
        )

        // context.assets를 호출하면 assetManager를 반환하도록 설정
        whenever(context.assets).thenReturn(assetManager)
    }

    /**
     * 페이지 범위가 초과된 경우, 빈 리스트를 반환하는지 확인
     */
    @Test
    fun outOfRangePage_returnsEmptyList() = runTest {
        // given
        // 특정 구의 총 데이터 개수를 50으로
        val apiSource = ApiSource.GANGNAM
        val page = 10 // 페이지 범위가 초과되는 값 가정
        val perPage = 100
        val totalCount = 50 // 특정 구의 총 데이터 개수
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(totalCount)

        // when
        // 넘어선 페이지를 요청했을 때의 결과 (50개의 데이터를 가진 구에서 900~999의 데이터를 가져오려고 함.)
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // 결과가 성공이고, 반환된 리스트가 비어있는지 확인
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    /**
     * DB에 데이터가 있는 경우, API를 호출하지 않고 DB 데이터를 반환하는지 확인
     */
    @Test
    fun dbHasEnoughData_returnsDbDataImmediately() = runTest {
        // given
        // 특정 구의 총 데이터 개수를 50으로
        val apiSource = ApiSource.GANGNAM
        val page = 1
        val perPage = 10
        val totalCount = 50 // 특정 구의 총 데이터 개수
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(totalCount)

        // DB에서 수거함을 불러왔을 때 반환할 데이터
        val mockBinsInDb = listOf(
            ClothingBin(
                "GANGNAM1",
                "서울특별시 강남구 테헤란로57길 38 (역삼동, 동우빌라)",
                "37.5067486779",
                "127.0463745363",
                "GANGNAM"
            ),
            ClothingBin(
                "GANGNAM2",
                "서울특별시 강남구 테헤란로 53길 51 (역삼동)",
                "37.5068409001",
                "127.0443692184",
                "GANGNAM"
            )
        )
        // DB에서 가져올 때 mockBinsInDb을 반환
        whenever(clothingBinDao.getBinsByDistrict(apiSource.name, perPage, 0)).thenReturn(mockBinsInDb)

        // 이미 DB에 50개가 저장되어 있다고 가정
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(50)

        // when
        // DB에서 충분한 데이터를 읽을 수 있는 상황
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // 결과가 성공이고, API 호출 없이 DB에서 가져온 데이터가 그대로 반환되는지 확인
        assertTrue(result.isSuccess)
        val bins = result.getOrNull()
        assertNotNull(bins)
        assertEquals(2, bins!!.size)
        assertEquals("GANGNAM1", bins[0].id)
        assertEquals("GANGNAM2", bins[1].id)

        // fetchClothingBins가 호출되지 않아야함.
        verify(apiHandler, never()).fetchClothingBins(any(), any(), any(), any())
    }

    /**
     * DB에 충분한 데이터가 없을 때, API를 통해 데이터를 가져오고 DB에 저장하는지 확인
     */
    @Test
    fun dbInsufficient_fetchFromApiAndSave() = runTest {
        // given
        val apiSource = ApiSource.GANGNAM
        val page = 1
        val perPage = 10
        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(0)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // API 응답 생성
        val mockApiResponseBody = ClothingBinResponse(
            currentCount = 2,
            data = listOf(
                mapOf(
                    "연번" to 1,
                    "도로명 주소" to "서울특별시 강남구 테헤란로57길 38 (역삼동, 동우빌라)",
                    "위도" to "37.5067486779",
                    "경도" to "127.0463745363",
                    "지번주소" to "서울특별시 강남구 역삼1동 694-6"
                ),
                mapOf(
                    "연번" to 2,
                    "도로명 주소" to "서울특별시 강남구 테헤란로 53길 51 (역삼동)",
                    "위도" to "37.5068409001",
                    "경도" to "127.0443692184",
                    "지번주소" to "서울특별시 강남구 역삼1동 698-14"
                )
            ),
            formattedData = listOf(
                ClothingBin(
                    "GANGNAM1",
                    "서울특별시 강남구 테헤란로57길 38 (역삼동, 동우빌라)",
                    "37.5067486779",
                    "127.0463745363",
                    "GANGNAM"
                ),
                ClothingBin(
                    "GANGNAM2",
                    "서울특별시 강남구 테헤란로 53길 51 (역삼동)",
                    "37.5068409001",
                    "127.0443692184",
                    "GANGNAM"
                )
            ),
            matchCount = 2,
            page = 1,
            perPage = 2,
            totalCount = 2
        )
        val mockApiResponse = Response.success(mockApiResponseBody)
        whenever(
            apiHandler.fetchClothingBins(
                eq(apiSource),
                eq(page),
                eq(perPage),
                anyString()
            )
        ).thenReturn(mockApiResponse)

        // when
        // DB에 데이터가 부족해 API에서 데이터를 새로 가져옴
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // API 데이터를 DB에 저장한 뒤, 결과 반환
        assertTrue(result.isSuccess)
        val bins = result.getOrNull()
        assertNotNull(bins)
        assertEquals(2, bins!!.size)
        assertEquals("GANGNAM1", bins[0].id)
        assertEquals("GANGNAM2", bins[1].id)

        // DB에 데이터가 저장되고, districtDataCountDao도 업데이트되었는지 확인
        verify(clothingBinDao).insertBins(anyList())
        verify(districtDataCountDao).insertOrUpdateCount(eq("GANGNAM"), eq(2))
    }

    /**
     * API 호출이 실패할 경우, 실패 결과를 반환하는지 확인
     */
    @Test
    fun apiCallFails_returnsFailure() = runTest {
        // given
        val apiSource = ApiSource.GANGNAM
        val page = 1
        val perPage = 10

        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(0)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // API 호출 시 예외 발생
        whenever(
            apiHandler.fetchClothingBins(
                eq(apiSource),
                eq(page),
                eq(perPage),
                anyString()
            )
        ).thenThrow(RuntimeException("Network Error"))

        // when
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // 실패 결과가 반환되고, ResourceException 타입인지 확인
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ResourceException)
        assertEquals(R.string.error_unknown, (exception as ResourceException).errorResId)
        assertEquals(null, result.exceptionOrNull()?.message)
    }

    /**
     * CSV에서 데이터를 불러오고 Room에 저장하는지 확인
     */
    @Test
    fun csvInsufficient_loadAndSaveCsv() = runTest {
        // given
        val apiSource = ApiSource.NOWON
        val page = 1
        val perPage = 10
        val totalCount = 0 // 특정 구의 총 데이터 개수

        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(totalCount)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // CSV 파일 읽어오기
        val mockCsvStream = ByteArrayInputStream(
            """
            연번,동,위치,위도,경도
            1,월계1동,월계동 37-11,37.6162112,127.065991
            2,월계1동,월계동 21-6,37.6170034,127.0658265
        """.trimIndent().toByteArray()
        )
        // csv파일 읽어오기
        whenever(assetManager.open("bin_list_nowon.csv"))
            .thenReturn(mockCsvStream)

        // when
        // CSV 파일을 열어 데이터를 읽고 DB에 저장
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // CSV 데이터가 성공적으로 반환되는지 확인
        assertTrue(result.isSuccess)
        val bins = result.getOrNull()
        assertNotNull(bins)
        assertEquals(2, bins!!.size)
        assertEquals("NOWON1", bins[0].id)
        assertEquals("NOWON2", bins[1].id)

        // DB에 insert와 totalCount 업데이트가 이뤄졌는지 확인
        verify(clothingBinDao).insertBins(any())
        verify(districtDataCountDao).insertOrUpdateCount("NOWON", 2)
    }

    /**
     * CSV 파일을 찾을 수 없을 때, 예외가 발생하는지 확인
     */
    @Test
    fun csvFileNotFound_returnsFailure() = runTest {
        // given
        val apiSource = ApiSource.NOWON
        val page = 1
        val perPage = 10

        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(0)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // CSV 파일 열기 시 예외 발생
        whenever(assetManager.open("bin_list_nowon.csv")).thenThrow(FileNotFoundException("CSV 파일 없음"))

        // when
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // FileNotFoundException으로 실패했는지 확인
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FileNotFoundException)
        assertEquals("CSV 파일 없음", result.exceptionOrNull()?.message)
    }

    /**
     * 북마크 개수를 Flow로 반환할 때, 올바른 값이 반환되는지 확인
     */
    @Test
    fun bookmarkCountFlow_emitsCorrectValue() = runTest {
        // given
        // 북마크 개수를 Flow<Int>형태로 5를 반환한다고 가정
        whenever(clothingBinDao.getBookmarkBinsCount()).thenReturn(flowOf(5))

        // when
        val flow = repository.getBookmarkBinsCount()

        // then
        // 실제 반환값이 5인지 확인
        val job = launch {
            flow.collect { count ->
                // 첫 번째(그리고 유일한) emit 값이 5여야 한다
                assertEquals(5, count)
            }
        }
        job.cancel()
    }

    /**
     * 모든 북마크된 의류 수거함 리스트를 Flow로 반환할 때, 올바른 목록이 반환되는지 확인
     */
    @Test
    fun allBookmarkedBins_emitsCorrectList() = runTest {
        // given
        // 북마크돈 수거함을 Flow<List<ClothingBin>> 형태로 반환한다고 가정
        val mockBookmarkedBins = listOf(
            ClothingBin(
                "GANGNAM1",
                "서울특별시 강남구 테헤란로57길 38 (역삼동, 동우빌라)",
                "37.5067486779",
                "127.0463745363",
                "GANGNAM"
            ),
            ClothingBin(
                "GANGNAM2",
                "서울특별시 강남구 테헤란로 53길 51 (역삼동)",
                "37.5068409001",
                "127.0443692184",
                "GANGNAM"
            )
        )
        whenever(clothingBinDao.getAllBookmarkedBins()).thenReturn(flowOf(mockBookmarkedBins))

        // when
        val flow = repository.getAllBookmarkedBins()

        // then
        // 실제 반환값이 mockBookmarkedBins와 일치하는지
        val job = launch {
            flow.collect { bins ->
                assertEquals(2, bins.size)
                assertEquals("GANGNAM1", bins[0].id)
                assertEquals("GANGNAM2", bins[1].id)
            }
        }
        job.cancel()
    }

    /**
     * 북마크가 안 되어있을 경우, 토글 시 북마크가 추가되는지 확인
     */
    @Test
    fun toggleBookmark_whenNotBookmarked_addsBookmark() = runTest {
        // given
        val binId = "GANGNAM1"
        // 해당 아이디의 북마크 상태 false로 북마크가 안 되어있음
        whenever(bookmarkDao.toggleBookmark(binId)).thenReturn(false)

        // when
        // 북마크 추가
        val result = repository.toggleBookmark(binId)

        // then
        // 북마크 추가 성공 여부와 DB 업데이트 호출 확인
        assertEquals(BookmarkType.ADD_SUCCESS, result)
        verify(clothingBinDao).updateBookmarkStatus(binId, true)
    }

    /**
     * 이미 북마크되어 있을 경우, 토글 시 북마크가 삭제되는지 확인
     */
    @Test
    fun toggleBookmark_whenBookmarked_removesBookmark() = runTest {
        // given
        val binId = "GANGNAM1"
        // 해당 아이디의 북마크 상태 true로 북마크가 되어있음
        whenever(bookmarkDao.toggleBookmark(binId)).thenReturn(true)

        // when
        // 북마크 제거
        val result = repository.toggleBookmark(binId)

        // then
        // 북마크 제거 성공 여부와 DB 업데이트 호출 확인
        assertEquals(BookmarkType.REMOVE_SUCCESS, result)
        verify(clothingBinDao).updateBookmarkStatus(binId, false)
    }

    /**
     * getTotalPage 호출 시 한 번만 DB에서 값을 가져오고, 이후에는 캐싱된 값을 사용하는지 확인
     */
    @Test
    fun getTotalPage_cachesResult() = runTest {
        // given
        // 특정 구의 총 데이터 개수를 55로 가정
        val apiSource = ApiSource.GANGNAM
        val perPage = 10
        val totalCount = 55
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(totalCount)

        // when
        // 첫 호출 시 DB에서 55를 읽어와 6페이지로 계산 (ceil(55/10)=6)
        val totalPage1 = repository.getTotalPage(apiSource, perPage)
        // 두 번째 호출은 캐시 사용, DB 조회 없이 이전 값(6) 그대로
        val totalPage2 = repository.getTotalPage(apiSource, perPage)

        // then
        // DB 조회가 한 번만 일어나는지 확인
        verify(districtDataCountDao, times(1)).getTotalCount(apiSource.name)
        // 두 번의 호출 결과가 같은지 확인
        assertEquals(6, totalPage1)
        assertEquals(6, totalPage2)
    }

    /**
     * 누락된 좌표가 있을 경우, API를 통해 좌표를 불러와 저장하는지 확인
     */
    @Test
    fun processMissingCoords_whenGeocodingSuccess_updatesCoordinates() = runTest {
        // given
        val apiSource = ApiSource.GANGNAM
        val page = 1
        val perPage = 10
        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(0)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // 누락된 좌표를 가진 수거함
        val binsWithMissingCoords = listOf(
            ClothingBin("GANGNAM1", "서울특별시 강남구 테헤란로57길 38", null, null, "GANGNAM")
        )
        val responseBody = ClothingBinResponse(
            currentCount = 1,
            data = emptyList(),
            formattedData = binsWithMissingCoords,
            matchCount = 1,
            page = 1,
            perPage = 10,
            totalCount = 1
        )
        // API에서 받아온 응답
        whenever(apiHandler.fetchClothingBins(eq(apiSource), eq(page), eq(perPage), any())).thenReturn(Response.success(responseBody))

        // 지오코딩 결과
        val mockGeoResponse = GeocodingResponse(
            addresses = listOf(
                Address(
                    x = "127.0463925", y = "37.5067505",
                    roadAddress = null,
                    jibunAddress = null,
                    englishAddress = null,
                    addressElements = null,
                    distance = null
                )
            ),
            status = "OK",
            meta = null,
            errorMessage = null
        )
        // geocodingApi 호출 시 mockGeoResponse 반환
        whenever(geocodingApi.getCoordinates(anyString(), anyString(), anyString())).thenReturn(Response.success(mockGeoResponse))

        // when
        // 누락 좌표 보완한 결과가 반환
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // 성공적으로 좌표가 채워졌는지 확인
        assertTrue(result.isSuccess)
        val fetchedBins = result.getOrNull()
        assertNotNull(fetchedBins)
        assertEquals(1, fetchedBins!!.size)
        assertEquals("37.5067505", fetchedBins[0].latitude)
        assertEquals("127.0463925", fetchedBins[0].longitude)
    }

    /**
     * 지오코딩 실패 시, 좌표는 채워지지 않고 null 상태로 남는지 확인한다.
     */
    @Test
    fun processMissingCoords_whenGeocodingFails_coordsRemainNull() = runTest {
        // given
        val apiSource = ApiSource.GANGNAM
        val page = 1
        val perPage = 10
        // 해당 구의 총 데이터 개수와 DB에 저장된 데이터 개수를 0으로
        whenever(districtDataCountDao.getTotalCount(apiSource.name)).thenReturn(0)
        whenever(clothingBinDao.getStoredCountForDistrict(apiSource.name)).thenReturn(0)

        // 누락된 좌표를 가진 수거함
        val binsWithMissingCoords = listOf(
            ClothingBin("GANGNAM1", "서울특별시 강남구 테헤란로57길 38", null, null, "GANGNAM")
        )
        val responseBody = ClothingBinResponse(
            currentCount = 1,
            data = emptyList(),
            formattedData = binsWithMissingCoords,
            matchCount = 1,
            page = 1,
            perPage = 10,
            totalCount = 1
        )
        val response = Response.success(responseBody)
        whenever(apiHandler.fetchClothingBins(eq(apiSource), eq(page), eq(perPage), any())).thenReturn(response)

        // 지오코딩 API 호출 시 예외 발생
        whenever(geocodingApi.getCoordinates(anyString(), anyString(), anyString())).thenThrow(RuntimeException("Geocoding Error"))

        // when
        // 좌표 변환이 실패해도, 전체 로직은 성공(빈 좌표로 저장)
        val result = repository.getOrFetchBins(apiSource, page, perPage)

        // then
        // 결과 자체는 성공이나, 좌표는 여전히 null
        assertTrue(result.isSuccess)
        val fetchedBins = result.getOrNull()
        assertNotNull(fetchedBins)
        assertEquals(1, fetchedBins!!.size)

        // 좌표 변환이 실패했으므로, 여전히 null인지 확인
        val bin = fetchedBins[0]
        assertNull(bin.latitude)
        assertNull(bin.longitude)

        // DB insert와 totalCount도 업데이트가 호출되었는지 확인
        verify(clothingBinDao).insertBins(anyList())
        verify(districtDataCountDao).insertOrUpdateCount(eq("GANGNAM"), eq(1))
    }
}