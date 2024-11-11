## Hjemmeoppgave - backend - Entur

Her ligger min forslag til løsning av oppgave.

Oppgaven er løst med Groovy. Det er ikke trukket inn noen eksterne avhengigheter for løsing av selve oppgaven, men 
 jeg trakk inn [Spock](https://spockframework.org) som testrammeverk.

###  Oppsummering
* En Rute er en sammensetning av navn og stoppesteder. Et stoppested i en rute kan ha 
  flere tilknyttede stoppesteder med reisetid i mellom, representert vha. en strekning. 
  Tanken var at dette skulle kunne tilpasses til at et stoppested ikke bare var uni-/bidirectional,
  men også multidirectional. Fikk ikke helt tid til å implementere dette helt som jeg ønsket.
* Det er per nå ikke noe logisk tilknyttning mellom avganger på en slik måte jeg ønsker.
  Tanken var at en avgang fra et stoppested skal kunne knyttes opp mot avganger fra et annet stoppested,
  men akkurat nå er den ganske dum og en avgang fra f.eks. Lillehammer trenger ikke valideres mot avganger
  fra f.eks. Tangen (dersom det skulle vært flere tilknyttede avganger).
* Kalkulering av antall reisende og fortjeneste fungerer bare en vei, "fremover". Burde selvfølgelig fungere
  begge veier på samme måte som utregning av total reisetid mellom stasjoner.
* Rushtid og rolig periode er representert vha. en Tuple. Ikke helt det som er tanken bak en tudple, men det var en enkel
  og rett frem måte å representere en spesiell periode mellom to tidspunkter.
* Det at Kommune har kontroll på antall stoppesteder er ganske teit og burde nok ikke gjøres på den måten...
  Burde vært gjennomgått og potensielt endret på datamodellen her slik at man legger til stoppesteder inn i et
  Kommuneobjekt slik at det blir mer dynamisk.
* Kalkulering av reisepris er IKKE validert manuelt, da den ble kastet litt sammen i siste liten. Så forbehold om at
  verdier her kan være feil...

### FAQ
#### Hvor lang tid tok det?
Jeg brukte deler av søndagen på oppgaven (ca 5-6 timer) pluss noe tid på
mandag for å løse noen TODOs jeg hadde i bakhodet. Denne README ble også skrevet
på mandag. Det står i oppgaven at man kun skal bruke 2-3 timer, men fikk beskjed over telefon at jeg
kunne bruken den tiden jeg ønsket.

#### Det er ikke noe innhold i main, hvorfor det?
Jeg tok en test-first approach, slik som oppgaven forventet, og tenkte at testene kunne være en representasjon av hvordan
applikasjonen fungerer fremfor å legge inn en demo i main-klassen. Ved bruk av Spock og datadrevne tester føler
jeg at testene er lesbare nok til å forstå hvordan applikasjonen fungerer.
