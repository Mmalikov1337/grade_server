package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Subject(
	val name: String,
	val typeID: Int,
	override var id: Int = -1
) : Item

class SubjectsTable : ItemTable<Subject>() {
	override val primaryKey = PrimaryKey(id, name = "subject_id_pk")
	val name = varchar("name", 50)
	val typeID = integer("type").references(typesTable.id, onDelete = ReferenceOption.CASCADE)

	override fun fill(builder: UpdateBuilder<Int>, item: Subject) {
		builder[name] = item.name
		builder[typeID] = item.typeID
	}

	override fun readResult(result: ResultRow) =
		Subject(
			result[name],
			result[typeID],
			result[id].value
		)
}

val subjectsTable = SubjectsTable()