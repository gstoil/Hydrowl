package org.semanticweb.hydrowl;

public class QueriesForTestOntologies {
	
	public static String getFlyAnatomyQuery(int i) {
		String prefix = "";
		prefix+="http://purl.obolibrary.org/obo/";
		switch(i) {
		case 1:
			return "Q(?0) <- " +prefix+"FBbt_00005106(?0), "+ 
							prefix+"RO_0002131(?0,?1), "+
							prefix+"FBbt_00007401(?1)";
			
		case 2:
			return "Q(?0) <- "+prefix+"FBbt_00005106(?0), " +
							prefix+"RO_0002131(?0,?1), " +
							prefix+"FBbt_00007401(?1), " +
							prefix+"FBbt#develops_from(?0,?2), " +
//							prefix+"develops_from(?0,?2), " +
							prefix+"FBbt_00067346(?2)";
		case 3:
			return "Q(?0) <- "+prefix+"FBbt_00100388(?0)";
		case 4:
			return "Q(?0) <- "+prefix+"FBbt_00007173(?0), " +
							prefix+"RO_0002131(?0,?1), " +
							prefix+"FBbt_00003924(?1)";
		case 5:
			return "Q(?0) <- "+prefix+"RO_0002130(?0,?1), " +
							prefix+"FBbt_00007053(?1)";
		default:
			return null;
		}
	}
	public static String getHectorQuery(int i) {
		String prefixForA="file:/home/aurona/0AlleWerk/Navorsing/Ontologies/NAP/NAP#";
		String prefixForS="http://www.owl-ontologies.com/Ontology1207768242.owl#";
		String prefixForU="http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
		String prefixForV="http://vicodi.org/ontology#";
		switch(i) {
		//START: Queries for ontologies A and AX
		case 1:
			return "Q(?0) <- "+prefixForA+"Device(?0), http://ksg.meraka.co.za/adolena.owl#assistsWith(?0,?1)";
		case 2:
			return "Q(?0) <- "+prefixForA+"Device(?0), http://ksg.meraka.co.za/adolena.owl#assistsWith(?0,?1), "+prefixForA+"UpperLimbMobility(?1)";
		case 3:
			return "Q(?0) <- "+prefixForA+"Device(?0), http://ksg.meraka.co.za/adolena.owl#assistsWith(?0,?1), "+prefixForA+"Hear(?1), http://ksg.meraka.co.za/adolena.owl#affects(?2,?1), "+prefixForA+"Autism(?2)";
		case 4:
			return "Q(?0) <- "+prefixForA+"Device(?0), http://ksg.meraka.co.za/adolena.owl#assistsWith(?0,?1), "+prefixForA+"PhysicalAbility(?1)";
		case 5:
			return "Q(?0) <- "+prefixForA+"Device(?0), http://ksg.meraka.co.za/adolena.owl#assistsWith(?0,?1), "+prefixForA+"PhysicalAbility(?1), http://ksg.meraka.co.za/adolena.owl#affects(?2,?1), "+prefixForA+"Quadriplegia(?2)";
		//END: Queries for ontologies A and AX

		//START: Queries for ontology S
		case 6:
			return "Q(?0) <- "+prefixForS+"StockExchangeMember(?0)";
		case 7:
			return "Q(?0,?1) <- "+prefixForS+"Person(?0), "+prefixForS+"hasStock(?0,?1), "+prefixForS+"Stock(?1)";
		case 8:
			return "Q(?0,?1,?2) <- "+prefixForS+"FinantialInstrument(?0), "+prefixForS+"belongsToCompany(?0,?1), "+prefixForS+"Company(?1), "+prefixForS+"hasStock(?1,?2), "+prefixForS+"Stock(?2)";
		case 9:
			return "Q(?0,?1,?2) <- "+prefixForS+"Person(?0), "+prefixForS+"hasStock(?0,?1), "+prefixForS+"Stock(?1), "+prefixForS+"isListedIn(?1,?2), "+prefixForS+"StockExchangeList(?2)";
		case 10:
			return "Q(?0,?1,?2,?3) <- "+prefixForS+"FinantialInstrument(?0), "+prefixForS+"belongsToCompany(?0,?1), "+prefixForS+"Company(?1), "+prefixForS+"hasStock(?1,?2), "+prefixForS+"Stock(?2), "+prefixForS+"isListedIn(?1,?3), "+prefixForS+"StockExchangeList(?3)";
		//END: Queries for ontology S

		//START: Queries for ontologies U and UX
		case 11:
			return "Q(?0) <- "+prefixForU+"worksFor(?0,?1), "+prefixForU+"affiliatedOrganizationOf(?1,?2)";
		case 12:
			return "Q(?0,?1) <- "+prefixForU+"Person(?0), "+prefixForU+"teacherOf(?0,?1), "+prefixForU+"Course(?1)";
		case 13:
			return "Q(?0,?1,?2) <- "+prefixForU+"Student(?0), "+prefixForU+"advisor(?0,?1), "+prefixForU+"FacultyStaff(?1), "+prefixForU+"takesCourse(?0,?2), "+prefixForU+"teacherOf(?1,?2), "+prefixForU+"Course(?2)";
		case 14:
			return "Q(?0,?1) <- "+prefixForU+"Person(?0), "+prefixForU+"worksFor(?0,?1), "+prefixForU+"Organization(?1)";
		case 15:
			return "Q(?0) <- "+prefixForU+"Person(?0), "+prefixForU+"worksFor(?0,?1), "+prefixForU+"University(?1), "+prefixForU+"hasAlumnus(?1,?0)";
		//END: Queries for ontologies U and UX
			
		//START: Queries for ontology V
		case 16:
			return "Q(?0) <- "+prefixForV+"Location(?0)";
		case 17:
			return "Q(?0,?1) <- "+prefixForV+"Military-Person(?0), "+prefixForV+"hasRole(?1,?0), "+prefixForV+"related(?0,?2)";
		case 18:
			return "Q(?0,?1) <- "+prefixForV+"Time-Dependant-Relation(?0), "+prefixForV+"hasRelationMember(?0,?1), "+prefixForV+"Event(?1)";
		case 19:
			return "Q(?0,?1) <- "+prefixForV+"Object(?0), "+prefixForV+"hasRole(?0,?1), "+prefixForV+"Symbol(?1)";
		case 20:
			return "Q(?0) <- "+prefixForV+"Individual(?0), "+prefixForV+"hasRole(?0,?1), "+prefixForV+"Scientist(?1), "+prefixForV+"hasRole(?0,?2), "+prefixForV+"Discoverer(?2), "+prefixForV+"hasRole(?0,?3), "+prefixForV+"Inventor(?3)";			
		default:
			return null;
		}
	}
	public static String getLUBMQuery(int i) {
		String uriPrefix=null;
		uriPrefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
//		uriPrefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
		switch (i) {
		case 1:
			return "Q(?0) <- "+uriPrefix+"GraduateStudent(?0), "+uriPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)";
		case 2:
			return "Q(?0,?1,?2) <- "+uriPrefix+"GraduateStudent(?0), "+uriPrefix+"memberOf(?0,?2),"+uriPrefix+"undergraduateDegreeFrom(?0,?1)"
									+uriPrefix+"University(?1), "+uriPrefix+"Department(?2), "+uriPrefix+"subOrganizationOf(?2,?1)";
		case 3:
			return "Q(?0) <- "+uriPrefix+"Publication(?0), "+uriPrefix+"publicationAuthor(?0,http://www.Department0.University0.edu/AssistantProfessor0)";
		case 4:
			return "Q(?0,?1,?2,?3) <- "+uriPrefix+"Professor(?0), "+uriPrefix+"worksFor(?0,http://www.Department0.University0.edu), "+uriPrefix+"name(?0,?1), "+uriPrefix+"emailAddress(?0,?2), "+uriPrefix+"telephone(?0, ?3)";
		case 5:
			return "Q(?0) <- "+uriPrefix+"Person(?0), "+uriPrefix+"memberOf(?0, http://www.Department0.University0.edu)";
		case 6:
			return "Q(?0) <- "+uriPrefix+"Student(?0)";
		case 7:
			return "Q(?0,?1) <- "+uriPrefix+"Student(?0), "+uriPrefix+"takesCourse(?0,?1)"+uriPrefix+"Course(?1), "+uriPrefix+"teacherOf(http://www.Department0.University0.edu/AssociateProfessor0, ?1), ";
		case 8:
			return "Q(?0,?1,?2) <- "+uriPrefix+"Student(?0), "+uriPrefix+"memberOf(?0,?1), "+uriPrefix+"Department(?1), "+uriPrefix+"subOrganizationOf(?1, http://www.University0.edu), "+uriPrefix+"emailAddress(?0,?2)";
		case 9:
			return "Q(?0,?1,?2) <- "+uriPrefix+"Student(?0), "+uriPrefix+"advisor(?0, ?1), "+uriPrefix+"Faculty(?1), "+uriPrefix+"takesCourse(?0,?2), "+uriPrefix+"Course(?2), "+uriPrefix+"teacherOf(?1,?2)";
		case 10:
			return "Q(?0) <- "+uriPrefix+"Student(?0), "+uriPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)";
		case 11:
			return "Q(?0) <- "+uriPrefix+"ResearchGroup(?0), "+uriPrefix+"subOrganizationOf(?0,http://www.University0.edu)";
		case 12:
			return "Q(?0,?1) <- "+uriPrefix+"Chair(?0), "+uriPrefix+"Department(?1), "+uriPrefix+"worksFor(?0,?1), "+uriPrefix+"subOrganizationOf(?1,http://www.University0.edu)";
		case 13:
			return "Q(?0) <- "+uriPrefix+"Person(?0), "+uriPrefix+"hasAlumnus(http://www.University0.edu, ?0)";
		case 14:
			return "Q(?0) <- "+uriPrefix+"UndergraduateStudent(?0)";
		default:
			return null;
		}
	}
	
	public static String getUOBMQuery(int i) {
		String uriPrefix = "http://172.16.83.69/univ-bench-dl.owl#";
//		uriPrefix = "http://uob.iodt.ibm.com/univ-bench-dl.owl#";
		switch (i) {
		case 1:
			return "Q(?0) <- "+uriPrefix+"UndergraduateStudent(?0), "+uriPrefix+"takesCourse(?0, http://www.Department0.University0.edu/Course0)";
		case 2:
			return "Q(?0) <- "+uriPrefix+"Employee(?0)";
		case 3:
			return "Q(?0) <- "+uriPrefix+"Student(?0), "+uriPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)";
		case 4:
			return "Q(?0,?1) <- "+uriPrefix+"Publication(?0), "+uriPrefix+"publicationAuthor(?0, ?1), "+uriPrefix+"Faculty(?1), "+uriPrefix+"isMemberOf(?1,http://www.Department0.University0.edu)";
		case 5:
			return "Q(?0) <- "+uriPrefix+"ResearchGroup(?0), "+uriPrefix+"subOrganizationOf(?0,http://www.University0.edu)";
		case 6:
			return "Q(?0) <- "+uriPrefix+"Person(?0), "+uriPrefix+"hasAlumnus(http://www.University0.edu,?0)";
		case 7:
			return "Q(?0) <- "+uriPrefix+"Person(?0), "+uriPrefix+"hasSameHomeTownWith(?0,http://www.Department0.University0.edu/FullProfessor0)";
		case 8:
			return "Q(?0) <- "+uriPrefix+"SportsLover(?0), "+uriPrefix+"hasMember(http://www.Department0.University0.edu,?0)";
		case 9:
			return "Q(?0,?1,?2) <- "+uriPrefix+"GraduateCourse(?0), "+uriPrefix+"isTaughtBy(?0,?1), "+uriPrefix+"isMemberOf(?1,?2), "+uriPrefix+"subOrganizationOf(?2,http://www.University0.edu)";
		case 10:
			return "Q(?0) <- "+uriPrefix+"isFriendOf(?0,http://www.Department0.University0.edu/FullProfessor0)";
		case 11:
			return "Q(?0,?1,?2) <- "+uriPrefix+"Person(?0), "+uriPrefix+"like(?0,?1)"+uriPrefix+"like(?2,?1)"+uriPrefix+"isHeadOf(?2,http://www.Department0.University0.edu)"+uriPrefix+"Chair(?2)";
		case 12:
			return "Q(?0,?1) <- "+uriPrefix+"Student(?0), "+uriPrefix+"takesCourse(?0,?1), "+uriPrefix+"isTaughtBy(?1,http://www.Department0.University0.edu/FullProfessor0)";
		case 13:
			return "Q(?0) <- "+uriPrefix+"PeopleWithHobby(?0), "+uriPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)";
		case 14:
			return "Q(?0,?1) <- "+uriPrefix+"Woman(?0), "+uriPrefix+"Student(?0), "+uriPrefix+"isMemberOf(?0,?1)"+uriPrefix+"subOrganizationOf(?1,http://www.University0.edu)";
		case 15:
			return "Q(?0) <- "+uriPrefix+"PeopleWithManyHobbies(?0), "+uriPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)";
		default:
			return null;
		}
	}
	
	public static String getLUBMExtQuery(int i) {
		String uriPrefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
		switch(i) {
		case 1: 
/*req1*/	return "Q(?0) <- " + uriPrefix+"worksFor(?0,?1), " + uriPrefix+"affiliatedOrganizationOf(?1,?2)";
		case 2:
/*req2*/	return "Q(?0,?1) <- " + uriPrefix+"Person(?0), " + uriPrefix+"teacherOf(?0,?1), " + uriPrefix+"Course(?1)";
		case 3:
/*req3*/	return "Q(?0,?1,?2) <- " + uriPrefix+"Student(?0), " + uriPrefix+"advisor(?0,?1), " + uriPrefix+"Faculty(?1), " + uriPrefix+"takesCourse(?0,?2),"
										+ uriPrefix+"teacherOf(?1,?2), " + uriPrefix+"Course(?2)";
		case 4:
/*req4*/	return "Q(?0,?1) <- " + uriPrefix+"Person(?0), " + uriPrefix+"worksFor(?0,?1), " + uriPrefix+"Organization(?1)";
		case 5:
/*req5*/	return "req5(?0) <- " + uriPrefix+"Person(?0), " + uriPrefix+"worksFor(?0,?1), " + uriPrefix+"University(?1), " + uriPrefix+"hasAlumnus(?1,?0)";
		case 6:
			return "q1(?0,?2) <- " + uriPrefix+"Student(?0), " + uriPrefix+"takesCourse(?0,?1), " + uriPrefix+"Course(?1), " + uriPrefix+"teacherOf(?2,?1), " +
									uriPrefix+"Faculty(?2), " + uriPrefix+"worksFor(?2,?3), " + uriPrefix+"Department(?3), " + uriPrefix+"memberOf(?0,?3)";
		case 7:
			return "q2(?0,?1) <- " + uriPrefix+"Subj3Student(?0), " + uriPrefix+"Subj4Student(?1), " + uriPrefix+"takesCourse(?0,?2), " + uriPrefix+"takesCourse(?1,?2)";
		case 8:
			return "q3(?0) <- " + uriPrefix+"Faculty(?0), " + uriPrefix+"degreeFrom(?0,?1), " + uriPrefix+"University(?1), " + uriPrefix+"subOrganizationOf(?2,?1), " +
								uriPrefix+"Department(?2), " + uriPrefix+"memberOf(?0,?2)";
		case 9:
			return "q4(?1,?4) <- " + uriPrefix+"Subj3Department(?1), " + uriPrefix+"Subj4Department(?4), " + uriPrefix+"Professor(?0), " + uriPrefix+"memberOf(?0,?1), " +
								 uriPrefix+"publicationAuthor(?2,?0), " + uriPrefix+"Professor(?3), " + uriPrefix+"memberOf(?3,?4), " + uriPrefix+"publicationAuthor(?2,?3)";
		case 10:
			return "q5(?0) <- " + uriPrefix+"Publication(?0), " + uriPrefix+"publicationAuthor(?0,?1), " + uriPrefix+"Professor(?1), " + uriPrefix+"publicationAuthor(?0,?2), " 
								+ uriPrefix+"Student(?2)";
		case 11:
			return "q6(?0,?1) <- " + uriPrefix+"University(?0), " + uriPrefix+"University(?1), " + uriPrefix+"memberOf(?2,?0), " + uriPrefix+"Student(?2), " + uriPrefix+"University(?1), " 
									+ uriPrefix+"memberOf(?3,?1), " + uriPrefix+"Professor(?3), " + uriPrefix+"advisor(?2,?3)";
		case 12:
			return "Q(?0) <- " + uriPrefix+"Publication(?0), " + uriPrefix+"Professor(?11), " + uriPrefix+"publicationAuthor(?0,?11), " + uriPrefix+"worksFor(?11,?12),"
								+ uriPrefix+"Subj1Department(?12), " + uriPrefix+"Professor(?21), " + uriPrefix+"publicationAuthor(?0,?21), " + uriPrefix+"worksFor(?21,?22),"
								+ uriPrefix+"Subj2Department(?22), " + uriPrefix+"Professor(?31), " + uriPrefix+"publicationAuthor(?0,?31), " + uriPrefix+"worksFor(?31,?32),"
								+ uriPrefix+"Subj3Department(?32)";
		case 13:
			return "Q(?0) <- " + uriPrefix+"Publication(?0), " + uriPrefix+"Subj1Professor(?11), " + uriPrefix+"publicationAuthor(?0,?11), " + uriPrefix+"worksFor(?11,?12),"
								+ uriPrefix+"Department(?12), " + uriPrefix+"Subj2Professor(?21), " + uriPrefix+"publicationAuthor(?0,?21), " + uriPrefix+"worksFor(?21,?22),"
								+ uriPrefix+"Department(?22), " + uriPrefix+"Subj3Professor(?31), " + uriPrefix+"publicationAuthor(?0,?31), " + uriPrefix+"worksFor(?31,?32),"
								+ uriPrefix+"Department(?32)";
		case 14: 
			return "Q(?10) <- " + uriPrefix+"publicationAuthor(?31,?10), " + uriPrefix+"publicationAuthor(?31,?11), " + uriPrefix+"JournalArticle(?31), " 
								+ uriPrefix+"publicationAuthor(?32,?11), " + uriPrefix+"publicationAuthor(?32,?12), " + uriPrefix+"JournalArticle(?32), " 
								+ uriPrefix+"teacherOf(?10,?20), " + uriPrefix+"Subj6Course(?20), " + uriPrefix+"worksFor(?10,?50), " + uriPrefix+"Subj6Department(?50), " 
								+ uriPrefix+"Subj6Student(?00), " + uriPrefix+"GraduateStudent(?00), " + uriPrefix+"takesCourse(?00,?20), " + uriPrefix+"publicationAuthor(?3,?00), " 
								+ uriPrefix+"JournalArticle(?3), " + uriPrefix+"teacherOf(?11,?21), " + uriPrefix+"Subj6Course(?21), " + uriPrefix+"worksFor(?11,?51), " 
								+ uriPrefix+"Subj6Department(?51), " + uriPrefix+"Subj6Student(?01), " + uriPrefix+"GraduateStudent(?01), " + uriPrefix+"takesCourse(?01,?21), " 
								+ uriPrefix+"AssociateProfessor(?11), " + uriPrefix+"GraduateCourse(?21), " + uriPrefix+"teacherOf(?12,?22), " + uriPrefix+"worksFor(?12,?52), " 
								+ uriPrefix+"GraduateStudent(?02), " + uriPrefix+"takesCourse(?02,?22), " + uriPrefix+"GraduateCourse(?22), " + uriPrefix+"memberOf(?12,?42), " 
								+ uriPrefix+"Institute(?42), " + uriPrefix+"GraduateCourse(?22), " + uriPrefix+"publicationAuthor(?33,?12), " + uriPrefix+"JournalArticle(?33)";
		default:
			return null;
		}
		
	}

}
