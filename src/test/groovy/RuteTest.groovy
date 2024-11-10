import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalTime

class RuteTest extends Specification {

    @Shared def R10 = new Rute("R10")

    @Shared def lillehammer = new Stoppested("Lillehammer", Kommune.LILLEHAMMER, 0.01)
    @Shared def moelv = new Stoppested("Moelv", Kommune.RINGSAKER, 0.01)
    @Shared def brumunddal = new Stoppested("Brumunddal", Kommune.RINGSAKER, 0.01,
            new Tuple(LocalTime.of(6, 0), LocalTime.of(8, 0))
    )
    @Shared def stange = new Stoppested("Stange", Kommune.STANGE, 0.01)
    @Shared def tangen = new Stoppested("Tangen", Kommune.STANGE, 0.01)
    @Shared def OSL = new Stoppested("Oslo Lufthavn", Kommune.ULLENSAKER, 0.02)
    @Shared def oslos = new Stoppested("Oslo S", Kommune.OSLO, 0.0)
    @Shared def grorud = new Stoppested("Grorud", Kommune.OSLO, 0.0)

    def setupSpec() {
        lillehammer.leggTilAvganger(oslos, [
                LocalTime.of(5, 0),
                LocalTime.of(5, 30),
                LocalTime.of(6, 0),
                LocalTime.of(6, 30),
                LocalTime.of(7, 0),
                LocalTime.of(10, 30),
        ])
        moelv.leggTilAvganger(oslos, [
                LocalTime.of(6, 30),
                LocalTime.of(7, 0)
        ])
        stange.leggTilAvganger(lillehammer,[
                LocalTime.of(6, 02),
                LocalTime.of(6, 42),
                LocalTime.of(7, 22),
        ])
        stange.leggTilAvganger(oslos,[
                LocalTime.of(5, 56),
                LocalTime.of(6, 26),
                LocalTime.of(6, 56),
                LocalTime.of(7, 26),
        ])
    }

    @Unroll
    def 'finnReisetid: skal være #forventetReisetid mellom #fraDestinasjon og #tilDestinasjon'() {

        given:
            R10.leggTilStoppested(lillehammer, moelv, 30)
            R10.leggTilStoppested(moelv, brumunddal, 30)
            R10.leggTilStoppested(brumunddal, stange, 20)
            R10.leggTilStoppested(stange, tangen, 20)
            R10.leggTilStoppested(tangen, OSL, 20)
            R10.leggTilStoppested(OSL, oslos, 30)
        when:
            def reisetid = R10.finnReisetid(fraDestinasjon, tilDestinasjon)
        then:
            reisetid == forventetReisetid
        where:
            fraDestinasjon | tilDestinasjon | forventetReisetid | beskrivelse
            lillehammer    | moelv          | 30                | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"
            lillehammer    | oslos          | 150               | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"
            oslos          | lillehammer    | 150               | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"
            moelv          | lillehammer    | 30                | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"
            lillehammer    | grorud         | -1                | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"
            lillehammer    | tangen         | 100               | "Reisetid fra ${fraDestinasjon.navn} til ${tilDestinasjon.navn}"

    }

    @Unroll
    def 'hentAvganger: skal hente avganger på stoppested gitt tidspunkt'() {

        given:
            R10.leggTilStoppested(lillehammer, moelv, 30)
        when:
            def avganger = R10.hentAvganger(tidspunkt, stoppested)
        then:
            avganger.collect { it.avganger }.flatten().size() == forventedeAntallAvganger
            avganger.collect { it.stasjon }.containsAll(endestasjoner)
        where:
            stoppested  | tidspunkt | forventedeAntallAvganger | endestasjoner
            lillehammer | "0545"    | 2                        | [oslos]
            lillehammer | "0715"    | 5                        | [oslos]
            lillehammer | "0430"    | 0                        | []
            stange      | "0630"    | 3                        | [oslos, lillehammer]
            stange      | "0400"    | 0                        | []
            stange      | "0815"    | 7                        | [oslos, lillehammer]
            brumunddal  | "0815"    | 0                        | []
    }

    @Unroll
    def 'hentAntallReisendeForAvgang: skal kunne hente korrekt antall reisende'() {
        when:
            def antallReisende = stoppested.hentAntallReisendeForAvgang(avgangstid)
        then:
            antallReisende == forventetAntallReisende
        where:
            stoppested  | avgangstid             | forventetAntallReisende
            lillehammer | LocalTime.of(6 ,0)     | 300
            OSL         | LocalTime.of(6 ,0)     | 800
            oslos       | LocalTime.of(6 ,0)     | 0
            brumunddal  | LocalTime.of(5 ,0)     | 175
            brumunddal  | LocalTime.of(6 ,0)     | 350
            brumunddal  | LocalTime.of(7 ,15)    | 350
            moelv       | LocalTime.of(6 ,0)     | 175
    }

    @Unroll
    def 'hentAntallReisende: skal kunne hente alle reisende for en avgang på et gitt tidspunkt'() {
        given:
            R10.leggTilStoppested(lillehammer, moelv, 30)
            R10.leggTilStoppested(moelv, brumunddal, 30)
            R10.leggTilStoppested(brumunddal, stange, 20)
            R10.leggTilStoppested(stange, tangen, 20)
            R10.leggTilStoppested(tangen, OSL, 20)
            R10.leggTilStoppested(OSL, oslos, 30)
        when:
            def antallReisende = R10.hentAntallReisende(tidspunkt, fraStasjon, tilStasjon)
        then:
            antallReisende == forventetAntallReisende
        where:
            fraStasjon  | tilStasjon | tidspunkt | forventetAntallReisende | beskrivelse
            lillehammer | oslos      | "0600"    | 2025                    | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt (i rush)"
            lillehammer | oslos      | "1030"    | 1850                    | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt (utenfor rush)"
            moelv       | oslos      | "0630"    | 1725                    | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt"
            moelv       | tangen     | "0630"    | 925                     | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt"
    }

}
