import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalTime

class RuteTest extends Specification {

    @Shared def R10 = new Rute("R10")

    @Shared def lillehammer = new Stoppested("Lillehammer", Kommune.LILLEHAMMER)
    @Shared def moelv = new Stoppested("Moelv", Kommune.RINGSAKER)
    @Shared def brumunddal = new Stoppested("Brumunddal", Kommune.RINGSAKER)
    @Shared def stange = new Stoppested("Stange", Kommune.STANGE)
    @Shared def tangen = new Stoppested("Tangen", Kommune.STANGE)
    @Shared def OSL = new Stoppested("Oslo Lufthavn", Kommune.ULLENSAKER)
    @Shared def oslos = new Stoppested("Oslo S", Kommune.OSLO)
    @Shared def grorud = new Stoppested("Grorud", Kommune.OSLO)

    def setupSpec() {
        lillehammer.leggTilAvganger(oslos, [
                LocalTime.of(6, 0),
                LocalTime.of(6, 30),
                LocalTime.of(7, 0),
                LocalTime.of(7, 30),
                LocalTime.of(8, 0),
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
            avganger.size() == forventedeAntallAvganger
        where:
            stoppested  | tidspunkt | forventedeAntallAvganger | beskrivelse
            lillehammer | "0645"    | 2                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            lillehammer | "0815"    | 5                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            lillehammer | "0530"    | 0                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            stange      | "0630"    | 3                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            stange      | "0400"    | 0                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            stange      | "0815"    | 7                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
            brumunddal  | "0815"    | 0                        | "Antall avganger fra ${stoppested.navn} innen en time fra $tidspunkt"
    }

}
