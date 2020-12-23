package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Student(
	val name: String,
	val groupID: Int,
	override var id: Int = -1
) : Item

class StudentsTable : ItemTable<Student>() {
	override val primaryKey = PrimaryKey(id, name = "student_id_pk")
	val name = varchar("name", 50)
	val groupID = integer("groupid")

	override fun fill(builder: UpdateBuilder<Int>, item: Student) {
		builder[name] = item.name
		builder[groupID] = item.groupID
	}

	override fun readResult(result: ResultRow) =
		Student(
			result[name],
			result[groupID],
			result[id].value
		)
}

val studentsTable = StudentsTable()