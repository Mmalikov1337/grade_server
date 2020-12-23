import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repo.*
import rest.restRepo

fun Application.myapp() {
	Database.connect("jdbc:h2:tcp://localhost/~/test", "org.h2.Driver", user = "sa", password = "1")

	install(ContentNegotiation) {
		json()
	}

	install(CORS) {
		method(HttpMethod.Options)
		method(HttpMethod.Get)
		method(HttpMethod.Post)
		method(HttpMethod.Put)
		method(HttpMethod.Delete)
		method(HttpMethod.Patch)
		header(HttpHeaders.AccessControlAllowHeaders)
		header(HttpHeaders.ContentType)
		header(HttpHeaders.AccessControlAllowOrigin)
		allowCredentials = true
		anyHost()
	}

	transaction {
		SchemaUtils.create(groupsTable)
		SchemaUtils.create(gradeTable)
		SchemaUtils.create(groupSubjectTable)
		SchemaUtils.create(studentsTable)
		SchemaUtils.create(subjectsTable)
		SchemaUtils.create(typesTable)
	}

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

fun main() {
	embeddedServer(
		Netty,
		watchPaths = listOf("gradleproject"),
		module = Application::myapp,
		port = 8080
	).start(wait = true)
}