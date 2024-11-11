import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalTime
import java.util.zip.DataFormatException

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
            R10.leggTilStoppested(stange, tangen, 30)
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
    def 'hentAvganger: skal kaste exception dersom tidspunkt ikke er gyldig'() {
        given:
            R10.leggTilStoppested(lillehammer, moelv, 30)
        expect:
            try {
                R10.hentAvganger(tidspunkt, stoppested)
                assert !forventetException
            } catch (Exception ignored) {
                assert forventetException
            }
        where:
            stoppested  | tidspunkt | forventetException  | beskrivelse
            lillehammer | "9999"    | DataFormatException | "Ugydlig tidspunkt $tidspunkt"
    }

    // Egentlig kun for min egen sanity...
    @Unroll
    def 'hentAntallReisendeForAvgang: skal simulere reise mellom Lillehammer og Oslo s'() {
        when:
            def antallReisende = stoppested.hentAntallReisendeForAvgang(avgangstid)
        then:
            antallReisende == forventetAntallReisende
        where:
            stoppested  | avgangstid             | forventetAntallReisende
            lillehammer | LocalTime.of(5 ,0)     | 300 // utenfor rush | 30_000 * 0.001     = 300
            moelv       | LocalTime.of(5 ,30)    | 175 // utenfor rush | 35_000 * 0.001 / 2 = 175
            brumunddal  | LocalTime.of(6 ,0)     | 350 // rush         | 35_000 * 0.002 / 2 = 350
            stange      | LocalTime.of(6 ,20)    | 200 // rush         | 20_000 * 0.002 / 2 = 200
            tangen      | LocalTime.of(6 ,40)    | 200 // rush         | 20_000 * 0.002 / 2 = 200
            OSL         | LocalTime.of(7 ,0)     | 800 // rush         | 40_000 * 0.002     = 800 | sum = 2025
    }

    @Unroll
    def 'hentAntallReisendeForAvgang: skal kunne hente korrekt antall reisende'() {
        when:
            def antallReisende = stoppested.hentAntallReisendeForAvgang(avgangstid)
        then:
            antallReisende == forventetAntallReisende
        where:
            stoppested  | avgangstid             | forventetAntallReisende | beskrivelse
            lillehammer | LocalTime.of(6 ,0)     | 600                     | "Avgang fra ${stoppested.navn} i rushtid"
            lillehammer | LocalTime.of(5 ,15)    | 300                     | "Avgang fra ${stoppested.navn} i rolig periode"
            lillehammer | LocalTime.of(12 ,15)   | 150                     | "Avgang fra ${stoppested.navn} utenfor rushtid"
            OSL         | LocalTime.of(10 ,0)    | 200                     | "Avgang fra ${stoppested.navn} i rolig periode"
            OSL         | LocalTime.of(8 ,45)    | 800                     | "Avgang fra ${stoppested.navn} i rushtid"
            brumunddal  | LocalTime.of(5 ,0)     | 175                     | "Avgang fra ${stoppested.navn} utenfor rushtid"
            brumunddal  | LocalTime.of(7 ,15)    | 350                     | "Avgang fra ${stoppested.navn} i rushtid"
            brumunddal  | LocalTime.of(12 ,15)   | 88                      | "Avgang fra ${stoppested.navn} i rolig periode"
            moelv       | LocalTime.of(5 ,30)    | 175                     | "Avgang fra ${stoppested.navn} utenfor rushtid"
            tangen      | LocalTime.of(6 ,15)    | 200                     | "Avgang fra ${stoppested.navn} i rushtid"
            tangen      | LocalTime.of(5 ,15)    | 100                     | "Avgang fra ${stoppested.navn} utenfor rushtid"
            tangen      | LocalTime.of(12 ,15)   | 50                      | "Avgang fra ${stoppested.navn} i rolig periode"
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
            lillehammer | oslos      | "0500"    | 2_025                   | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt (i rush)"
            lillehammer | oslos      | "1030"    | 626                     | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt (rolig)"
            lillehammer | oslos      | "1730"    | 1_250                   | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt (utenfor rush)"
            moelv       | oslos      | "0630"    | 1_900                   | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt"
            moelv       | tangen     | "0630"    | 900                     | "Antall reisende fra ${fraStasjon.navn} til ${tilStasjon.navn} med avgang $tidspunkt"
    }

    @Unroll
    def 'beregnFortjeneste: skal kunne beregne total fortjeneste for gitt avgang'() {
        given:
            R10.leggTilStoppested(lillehammer, moelv, 30)
            R10.leggTilStoppested(moelv, brumunddal, 30)
            R10.leggTilStoppested(brumunddal, stange, 20)
            R10.leggTilStoppested(stange, tangen, 20)
            R10.leggTilStoppested(tangen, OSL, 20)
            R10.leggTilStoppested(OSL, oslos, 30)
        when:
            def totalFortjeneste = R10.beregnFortjeneste(tidspunkt, avreisested)
        then:
            totalFortjeneste == forventetFortjeneste
        where:
            avreisested | tidspunkt | forventetFortjeneste | beskrivelse
            lillehammer | "0500"    | 587_500              | "Total fortjeneste fra ${avreisested.navn} utenfor rushtid"
            lillehammer | "0600"    | 855_000              | "Total fortjeneste fra ${avreisested.navn} i rushtid"
            lillehammer | "1030"    | 214_200              | "Total fortjeneste fra ${avreisested.navn} i rolig periode"
            moelv       | "0630"    | 685_000              | "Total fortjeneste fra ${avreisested.navn}"
            stange      | "0626"    | 540_000              | "Total fortjeneste fra ${avreisested.navn}"
    }

}
