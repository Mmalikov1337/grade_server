package model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.Date
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable
import java.time.LocalDate

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = LocalDate::class)
object DateSerializer : KSerializer<LocalDate> {

	override fun serialize(output: Encoder, obj: LocalDate) {
		output.encodeString(obj.toString())
	}

	override fun deserialize(input: Decoder): LocalDate {
		return LocalDate.parse(input.decodeString())
	}
}

//@Serializable
//open class Task (
//	open val name: String,
//	open val description: String,
//	open val difficulty: String = "MEDIUM",
//	val type: String,
//	open val characterId: Int,
//	@Serializable(with=DateSerializer::class)
//	open var deadline: LocalDate= LocalDate.MIN,
//	@Serializable(with=DateSerializer::class)
//	open val startDate: LocalDate= LocalDate.now(),
//	open var completionCount: Int= 0,
//)
@Serializable
class Grade(
	val studentID: Int,
	val subjectID: Int,
	var value: Int,
	@Serializable(with=DateSerializer::class)
	val date: LocalDate= LocalDate.now(),
	override var id: Int = -1
) : Item

class GradeTable : ItemTable<Grade>() {
	override val primaryKey = PrimaryKey(id, name = "grade_id_pk")
	val studentID = integer("studentid").references(subjectsTable.id, onDelete = ReferenceOption.CASCADE)
	val subjectID = integer("subjectid").references(studentsTable.id, onDelete = ReferenceOption.CASCADE)
	val date = date("date")
	val value = integer("value")

	override fun fill(builder: UpdateBuilder<Int>, item: Grade) {
		builder[studentID] = item.studentID
		builder[subjectID] = item.subjectID
		builder[date] = item.date
		builder[value] = if(item.value > 5) 5 else if(item.value < 1) 1 else item.value
	}

	override fun readResult(result: ResultRow) =
		Grade(
			result[studentID],
			result[subjectID],
			result[value],
			result[date],
			result[id].value
		)
}

val gradeTable = GradeTable()