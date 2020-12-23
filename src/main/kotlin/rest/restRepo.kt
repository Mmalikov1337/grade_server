package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import model.*
import org.jetbrains.exposed.sql.transactions.transaction
import repo.*

fun <T : Item> Application.restRepo(
	groupRepo: Repo<Group>,
	groupSerializer: KSerializer<Group>,

	gradeRepo: Repo<Grade>,
	gradeSerializer: KSerializer<Grade>,

	groupSubjectRepo: Repo<GroupSubject>,
	groupSubjectSerializer: KSerializer<GroupSubject>,

	studentRepo: Repo<Student>,
	studentSerializer: KSerializer<Student>,

	subjectRepo: Repo<Subject>,
	subjectSerializer: KSerializer<Subject>,

	typeRepo: Repo<Type>,
	typeSerializer: KSerializer<Type>,
) {
	routing {
		route("/init") {
			get {
				transaction {
					groupsTable.insertAndGetIdItem(Group("INITIAL")).value
					typesTable.insertAndGetIdItem(Type("INIT_TYPE")).value
					studentsTable.insertAndGetIdItem(Student("INIT_ST", 1)).value

					subjectsTable.insertAndGetIdItem(Subject("INIT_SUBJ", 1)).value
					gradeTable.insertAndGetIdItem(Grade(1, 1, 1)).value
					groupSubjectTable.insertAndGetIdItem(GroupSubject(1, 1)).value
					groupSubjectTable.insertAndGetIdItem(GroupSubject(1, 1)).value


					groupsTable.insertAndGetIdItem(Group("INITIAL2")).value
					typesTable.insertAndGetIdItem(Type("INIT_TYPE2")).value
					studentsTable.insertAndGetIdItem(Student("INIT_ST2", 2)).value

					subjectsTable.insertAndGetIdItem(Subject("INIT_SUBJ2", 2)).value
					gradeTable.insertAndGetIdItem(Grade(2, 2, 2)).value
					groupSubjectTable.insertAndGetIdItem(GroupSubject(2, 2)).value
					groupSubjectTable.insertAndGetIdItem(GroupSubject(2, 2)).value
					true
				}
				call.respond("init")
			}
		}

		route("/group") {
			post {
				parseBody(groupSerializer)?.let { elem ->
					if (groupRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Group created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Group")
			}
			get {
				val groups = groupRepo.all()
				if (groups.isNotEmpty()) {
					call.respond(groups)
				} else {
					call.respond(HttpStatusCode.NotFound, "Groups not found")
				}
			}
		}
		route("/group/{id}") {
			get {
				parseId()?.let { id ->
					groupRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Group with id=$id dont exist")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(groupSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (groupRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Group updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Group")
			}
			delete {
				parseId()?.let { id ->
					if (groupRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Group was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Group with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/group/{id}/students") {
			get {
				parseId()?.let { id ->
					groupRepo.all().find { it.id == id } ?: call.respond(
						HttpStatusCode.NotFound,
						"Group with id=$id not found"
					)
					studentRepo.all().filter { it.groupID == id }.let { elem ->
						call.respond(elem)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/group/subjects") {
			get {
				val groupSubjects = groupSubjectRepo.all()
				if (groupSubjects.isNotEmpty()) {
					call.respond(groupSubjects)
				} else {
					call.respond(HttpStatusCode.NotFound, "Subjects not found")
				}
			}
			post {
				parseBody(groupSubjectSerializer)?.let { elem ->
					if (groupSubjectRepo.create(elem))
						call.respond(HttpStatusCode.OK, "GroupSubject created")
					else
						call.respond(HttpStatusCode.NotFound, "$elem")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object GroupSubject")
			}
		}
		route("/group/{id}/subjects") {
			get {
				parseId()?.let { id ->
					groupRepo.all().find { it.id == id } ?: call.respond(
						HttpStatusCode.NotFound,
						"Group with id=$id not found"
					)
					groupSubjectRepo.all().filter { it.groupID == id }.let { elem ->
						call.respond(elem)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/subjects") {
			post {
				parseBody(subjectSerializer)?.let { elem ->
					if (subjectRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Subject was created")
					else
						call.respond(HttpStatusCode.NotFound)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Subject")
			}
			get {
				val subjects = subjectRepo.all()
				if (subjects.isNotEmpty()) {
					call.respond(subjects)
				} else {
					call.respond(HttpStatusCode.NotFound, "Subjects not found")
				}
			}
		}
		route("/subjects/{id}") {
			get {
				parseId()?.let { id ->
					subjectRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Subject with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(subjectSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (subjectRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Subject updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Subject")
			}
			delete {
				parseId()?.let { id ->
					if (subjectRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Subject was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Subject with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}

		route("/students") {
			post {
				parseBody(studentSerializer)?.let { elem ->
					if (studentRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Student was created")
					else
						call.respond(HttpStatusCode.NotFound)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Student")
			}
			get {
				val students = studentRepo.all()
				if (students.isNotEmpty()) {
					call.respond(students)
				} else {
					call.respond(HttpStatusCode.NotFound, "Student not found")
				}
			}
		}
		route("/students/{id}") {
			get {
				parseId()?.let { id ->
					studentRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Subject with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(studentSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (studentRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Student updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Student")
			}
//			post {
//				parseBody(studentSerializer)?.let { elem ->
//					if (studentRepo.create(elem))
//						call.respond(HttpStatusCode.OK, "Student created")
//					else
//						call.respond(HttpStatusCode.NotFound)
//				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Student")
//			}
			delete {
				parseId()?.let { id ->
					if (studentRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Student was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Subject with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/students/has={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value == parseQuery() }.map { it.studentID }.toList()
				val students = studentRepo.all().filter { grades.contains(it.id) }
				if (students.isNotEmpty()) {
					call.respond(students)
				} else {
					call.respond(HttpStatusCode.NotFound, "Student not found")
				}
			}
		}
		route("/students/more={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value > parseQuery()!! }.map { it.studentID }.toList()
				val students = studentRepo.all().filter { grades.contains(it.id) }
				if (students.isNotEmpty()) {
					call.respond(students)
				} else {
					call.respond(HttpStatusCode.NotFound, "Student not found")
				}
			}
		}
		route("/students/less={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value < parseQuery()!! }.map { it.studentID }.toList()
				val students = studentRepo.all().filter { grades.contains(it.id) }
				if (students.isNotEmpty()) {
					call.respond(students)
				} else {
					call.respond(HttpStatusCode.NotFound, "Student not found")
				}
			}
		}

		route("/grades") {
			post {
				parseBody(gradeSerializer)?.let { elem ->
					if (gradeRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Grades was created")
					else
						call.respond(HttpStatusCode.NotFound, "NotFound")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Grades")
			}
			get {
				val grades = gradeRepo.all()
				if (grades.isNotEmpty()) {
					call.respond(grades)
				} else {
					call.respond(HttpStatusCode.NotFound, "Grades not found")
				}
			}
		}
		route("/grades/{id}") {
			get {
				parseId()?.let { id ->
					gradeRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Grades with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(gradeSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (gradeRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Grades updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Grades")
			}
			delete {
				parseId()?.let { id ->
					if (gradeRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Grades was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Grades with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
		route("/grades/has={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value == parseQuery() }
				if (grades.isNotEmpty()) {
					call.respond(grades)
				} else {
					call.respond(HttpStatusCode.NotFound, "Grades not found")
				}
			}
		}
		route("/grades/more={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value > parseQuery()!! }
				if (grades.isNotEmpty()) {
					call.respond(grades)
				} else {
					call.respond(HttpStatusCode.NotFound, "Grades not found")
				}
			}
		}
		route("/grades/less={query}") {
			get {
				val grades = gradeRepo.all().filter { it.value < parseQuery()!! }
				if (grades.isNotEmpty()) {
					call.respond(grades)
				} else {
					call.respond(HttpStatusCode.NotFound, "Grades not found")
				}
			}
		}

		route("/types") {
			post {
				parseBody(typeSerializer)?.let { elem ->
					if (typeRepo.create(elem))
						call.respond(HttpStatusCode.OK, "Types was created")
					else
						call.respond(HttpStatusCode.NotFound)
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Types")
			}
			get {
				val types = typeRepo.all()
				if (types.isNotEmpty()) {
					call.respond(types)
				} else {
					call.respond(HttpStatusCode.NotFound, "Types not found")
				}
			}
		}
		route("/types/{id}") {
			get {
				parseId()?.let { id ->
					typeRepo.read(id)?.let { elem ->
						call.respond(elem)
					} ?: call.respond(HttpStatusCode.NotFound, "Types with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
			put {
				parseBody(typeSerializer)?.let { elem ->
					parseId()?.let { id ->
						if (typeRepo.update(id, elem))
							call.respond(HttpStatusCode.OK, "Types updated")
						else
							call.respond(HttpStatusCode.NotFound)
					}
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong object Types")
			}
			delete {
				parseId()?.let { id ->
					if (typeRepo.delete(id))
						call.respond(HttpStatusCode.OK, "Types was deleted")
					else
						call.respond(HttpStatusCode.NotFound, "Types with id=$id did not found")
				} ?: call.respond(HttpStatusCode.BadRequest, "Wrong id")
			}
		}
	}
}

fun PipelineContext<Unit, ApplicationCall>.parseId(id: String = "id") =
	call.parameters[id]?.toIntOrNull()

fun PipelineContext<Unit, ApplicationCall>.parseQuery(id: String = "query") =
	call.parameters[id]?.toIntOrNull()

suspend fun <T> PipelineContext<Unit, ApplicationCall>.parseBody(
	serializer: KSerializer<T>
) =
	try {
		Json.decodeFromString(
			serializer,
			call.receive()
		)
	} catch (e: Throwable) {
		null
	}
