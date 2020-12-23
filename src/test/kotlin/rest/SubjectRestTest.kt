package rest

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.json
import io.ktor.server.testing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import repo.RepoDSL
import repo.TestItem
import kotlin.test.Test
import kotlin.test.assertEquals


class SubjectRestTest {
	private val testPath = "/subjects"

	@Test
	fun restRepoMapTest() {
		testRest {
			restRepo<Item>(
				RepoDSL(groupsTable),
				Group.serializer(),
				RepoDSL(gradeTable),
				Grade.serializer(),
				RepoDSL(groupSubjectTable),
				GroupSubject.serializer(),
				RepoDSL(studentsTable),
				Student.serializer(),
				RepoDSL(subjectsTable),
				Subject.serializer(),
				RepoDSL(typesTable),
				Type.serializer()
			)
		}
	}


	private fun testRest(
		restModule: Application.() -> Unit
	) {
		withTestApplication({
			install(ContentNegotiation) {
				json()
			}
			Database.connect(
				"jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
				driver = "org.h2.Driver"
			)
			transaction {
				SchemaUtils.create(groupsTable)
				SchemaUtils.create(gradeTable)
				SchemaUtils.create(groupSubjectTable)
				SchemaUtils.create(studentsTable)
				SchemaUtils.create(subjectsTable)
				SchemaUtils.create(typesTable)

				groupsTable.insertAndGetIdItem(Group("INITIAL")).value
				typesTable.insertAndGetIdItem(Type("INIT_TYPE")).value
			}
			restModule()
		}) {
			println("\n--STARTED\n")
			// Post
			val itemsJson =
				arrayOf("one" to 1, "two"  to 1, "three" to 1)
					.map {
						Json.encodeToString(
							Subject.serializer(),
							Subject(it.first, it.second)
						)
					}

			itemsJson.map {
				println(it)
				handleRequest(HttpMethod.Post, testPath) {
					setBodyAndHeaders(it)
				}.apply {
					assertStatus(HttpStatusCode.OK)
				}
			}
			handleRequest(HttpMethod.Post, testPath) {
				setBodyAndHeaders("Wrong JSON")
			}.apply {
				assertStatus(HttpStatusCode.BadRequest)
			}
			println("\n--POST\n")
			// Get
			val users = handleRequest(HttpMethod.Get, testPath).run {
				assertStatus(HttpStatusCode.OK)
				parseResponse(
					ListSerializer(Subject.serializer())
				)
			}
			assertEquals(3, users?.size)
			var asd = handleRequest(HttpMethod.Get, "$testPath/${users?.first()?.id}").run {
				assertStatus(HttpStatusCode.OK)
				val user = parseResponse(Subject.serializer())
				assertEquals(users?.first()?.name, user?.name)
			}

			val items = handleRequest(HttpMethod.Get, testPath).run {
				assertStatus(HttpStatusCode.OK)
				parseResponse(
					ListSerializer(Subject.serializer())
				)
			}
			assertEquals(3, items?.size)
			handleRequest(HttpMethod.Get, "$testPath/${items?.first()?.id}").run {
				assertStatus(HttpStatusCode.OK)
				val item = parseResponse(Subject.serializer())
				assertEquals(items?.first()?.name, item?.name)
			}
			println("\n--GET\n")

			// Put
			val three = items?.find { it.name == "one" }!!
			val threeNew = Subject("one new", 1,three.id)
			println("\n--putin, ${three.id}\n")

			handleRequest(HttpMethod.Put, "$testPath/${threeNew.id}") {
				setBodyAndHeaders(Json.encodeToString(Subject.serializer(), threeNew))
			}.run {
				assertStatus(HttpStatusCode.OK)
			}
			println("\n--input\n")

			handleRequest(HttpMethod.Get, "$testPath/${threeNew.id}").run {
				assertStatus(HttpStatusCode.OK)
				val item = parseResponse(Subject.serializer())
				assertEquals("one new", item?.name)
			}
			println("\n--PUT\n")

			// Delete
			val two = items.find { it.name == "two" }!!
			handleRequest(HttpMethod.Delete, "$testPath/${two.id}").run {
				assertStatus(HttpStatusCode.OK)
			}

			println("\n--DELETE\n")
			// Final check
			val itemsNewName = handleRequest(HttpMethod.Get, testPath).run {
				assertStatus(HttpStatusCode.OK)
				parseResponse(
					ListSerializer(Subject.serializer())
				)
			}?.map { it.name }!!
			println("\n--FINAL\n")

			assert(itemsNewName.size == 2)
			assert(itemsNewName.contains("three"))
			assert(itemsNewName.contains("one new"))

		}
	}
}

//fun TestApplicationCall.assertStatus(status: HttpStatusCode) =
//	assertEquals(status, response.status())
//
//fun TestApplicationRequest.setBodyAndHeaders(body: String) {
//	setBody(body)
//	addHeader("Content-Type", "application/json")
//	addHeader("Accept", "application/json")
//}
//
//fun <T> TestApplicationCall.parseResponse(
//	serializer: KSerializer<T>
//) =
//	try {
//		Json.decodeFromString(
//			serializer,
//			response.content ?: ""
//		)
//	} catch (e: Throwable) {
//		null
//	}