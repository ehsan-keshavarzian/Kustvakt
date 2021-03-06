INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="base"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"Base structure layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="dereko"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"DeReKo structure layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="cnx"),
		(SELECT a.id FROM annotation as a WHERE a.description="Constituency"),
		"Connexor constituency layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="cnx"),
		(SELECT a.id FROM annotation as a WHERE a.description="Syntax"),
		"Connexor syntax layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="cnx"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"Connexor part of speech layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="cnx"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Connexor lemma layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="cnx"),
		(SELECT a.id FROM annotation as a WHERE a.description="Morphology"),
		"Connexor morphology layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="corenlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Constituency"),
		"CoreNLP constituency layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="corenlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"CoreNLP part of speech layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="corenlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"CoreNLP structure layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="corenlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Named entity"),
		"CoreNLP named entity layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="drukola"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"DruKoLa lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="drukola"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"DruKoLa part of speech layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="drukola"),
		(SELECT a.id FROM annotation as a WHERE a.description="Morphology"),
		"DruKoLa morphology layer"; 

		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="glemm"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Glemm lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="malt"),
		(SELECT a.id FROM annotation as a WHERE a.description="Dependency"),
		"Malt dependency layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="marmot"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"MarMot part of speech layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="marmot"),
		(SELECT a.id FROM annotation as a WHERE a.description="Morphology"),
		"MarMot morphology layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="mate"),
		(SELECT a.id FROM annotation as a WHERE a.description="Dependency"),
		"Mate dependency layer";
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="mate"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Mate lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="mate"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"Mate part of speech layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="mate"),
		(SELECT a.id FROM annotation as a WHERE a.description="Morphology"),
		"Mate morphology layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="mdp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Dependency"),
		"MD parser dependency layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="opennlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"OpenNLP part of speech layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="opennlp"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"OpenNLP structure layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="sgbr"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"Schreibgebrauch part of speech layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="sgbr"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Schreibgebrauch lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="sgbr"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma variant"),
		"Schreibgebrauch lemma variant layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="tt"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"Tree Tagger part of speech layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="tt"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Tree Tagger lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="tt"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"Tree Tagger structure layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="xip"),
		(SELECT a.id FROM annotation as a WHERE a.description="Lemma"),
		"Xerox Incremental Parser lemma layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="xip"),
		(SELECT a.id FROM annotation as a WHERE a.description="Structure"),
		"Xerox Incremental Parser structure layer"; 
		
INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="xip"),
		(SELECT a.id FROM annotation as a WHERE a.description="Part of speech"),
		"Xerox Incremental Parser part of speech layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="xip"),
		(SELECT a.id FROM annotation as a WHERE a.description="Constituency"),
		"Xerox Incremental Parser constituency layer"; 

INSERT INTO annotation_pair (annotation1, annotation2, description) 
	SELECT (SELECT a.id FROM annotation as a WHERE a.code="xip"),
		(SELECT a.id FROM annotation as a WHERE a.description="Dependency"),
		"Xerox Incremental Parser dependency layer"; 
