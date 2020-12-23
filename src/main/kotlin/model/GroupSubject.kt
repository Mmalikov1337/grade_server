package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

@Serializable
class GroupSubject(
	val groupID: Int,
	val subjectID: Int,
	override var id: Int = -1
) : Item

class GroupSubjectTable : ItemTable<GroupSubject>() {
	override val primaryKey = PrimaryKey(id, name = "group_subject_id_pk")
	val groupID = integer("groupid").references(groupsTable.id, onDelete = ReferenceOption.CASCADE)
	val subjectID = integer("subjectid").references(subjectsTable.id, onDelete = ReferenceOption.CASCADE)

	override fun fill(builder: UpdateBuilder<Int>, item: GroupSubject) {
		builder[groupID] = item.groupID
		builder[subjectID] = item.subjectID
	}

	override fun readResult(result: ResultRow) =
		GroupSubject(
			result[groupID],
			result[subjectID],
			result[id].value
		)
}

val groupSubjectTable = GroupSubjectTable()