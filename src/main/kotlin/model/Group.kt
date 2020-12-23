package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class Group(
	val name: String,
	override var id: Int = -1
) : Item

class GroupsTable : ItemTable<Group>() {
	override val primaryKey = PrimaryKey(id, name = "group_id_pk")
	val name = varchar("name", 50)

	override fun fill(builder: UpdateBuilder<Int>, item: Group) {
		builder[name] = item.name
	}

	override fun readResult(result: ResultRow) =
		Group(
			result[name],
			result[id].value
		)
}

val groupsTable = GroupsTable()