package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Grade(
	val studentID: Int,
	val subjectID: Int,
	val value: Int,
	override var id: Int = -1
) : Item

class GradeTable : ItemTable<Grade>() {
	override val primaryKey = PrimaryKey(id, name = "grade_id_pk")
	val studentID = integer("studentid").references(subjectsTable.id, onDelete = ReferenceOption.CASCADE)
	val subjectID = integer("subjectid").references(studentsTable.id, onDelete = ReferenceOption.CASCADE)
	val value = integer("value")

	override fun fill(builder: UpdateBuilder<Int>, item: Grade) {
		builder[studentID] = item.studentID
		builder[subjectID] = item.subjectID
		builder[value] = item.value
	}

	override fun readResult(result: ResultRow) =
		Grade(
			result[studentID],
			result[subjectID],
			result[value],
			result[id].value
		)
}

val gradeTable = GradeTable()