package grakndevops;

import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.concept.Attribute;
import ai.grakn.concept.AttributeType;
import ai.grakn.concept.Entity;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Relationship;
import ai.grakn.concept.RelationshipType;
import ai.grakn.concept.Role;
import ai.grakn.util.SimpleURI;

public class GraphCreator {

	public static void main(String[] args) {

		Grakn grakn = new Grakn(new SimpleURI("localhost:48555"));
		Grakn.Session session = grakn.session(Keyspace.of("grakndevsopssss"));
		Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);

		// Define schema

		AttributeType firstname = tx.putAttributeType("firstname", AttributeType.DataType.STRING);
		AttributeType surname = tx.putAttributeType("surname", AttributeType.DataType.STRING);

		Role spouse1 = tx.putRole("spouse1");
		Role spouse2 = tx.putRole("spouse2");
		RelationshipType marriage = tx.putRelationshipType("marriage").relates(spouse1).relates(spouse2);

		EntityType person = tx.putEntityType("person").plays(spouse1).plays(spouse2);

		person.has(firstname);
		person.has(surname);

		// Load data

		Attribute johnName = firstname.create("John"); // Create the attribute
		Attribute maryName = firstname.create("Mary");

		Entity john = person.create().has(johnName); // Link it to an entity
		Entity mary = person.create().has(maryName);

		// Create the actual relationship
		Relationship theMarriage = marriage.create().assign(spouse1, john).assign(spouse2, mary);

		tx.commit();
		System.out.println("...done!");
	}

}
