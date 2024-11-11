import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Stoppested {

    String navn
    Kommune kommune
    List<Avgang> avganger
    Tuple<LocalTime> rushPeriode = []
    Tuple<LocalTime> roligPeriode = []

    Stoppested(String navn, Kommune kommune) {
        this.navn = navn
        this.kommune = kommune
        this.avganger = []
        this.rushPeriode = [LocalTime.of(5, 59), LocalTime.of(9, 1)]
        this.roligPeriode = [LocalTime.of(8, 59), LocalTime.of(15, 1)]
    }

    def leggTilAvganger(Stoppested stoppested, List<LocalTime> avganger) {
        def existingAvgang = this.avganger.find { it.stasjon == stoppested }
        if (existingAvgang) existingAvgang.avganger.addAll(avganger)
        else this.avganger << new Avgang(stoppested, avganger)
    }

    def hentAntallReisendeForAvgang(LocalTime avgang) {
        def innbyggertallFordelt = Kommune.hentInnbyggertall(kommune) / Kommune.antallStoppesteder(kommune)
        if (erRushtid(avgang)) {
            return Math.ceil(innbyggertallFordelt * 0.02)
        }
        if (erRoligTid(avgang)) {
            return Math.ceil(innbyggertallFordelt * 0.005)
        }
        return Math.ceil(innbyggertallFordelt * 0.01)
    }

    private boolean erRushtid(LocalTime avgang) {
        return avgang.isAfter(rushPeriode.first)
                && avgang.isBefore(rushPeriode.last)
    }

    private boolean erRoligTid(LocalTime avgang) {
        return avgang.isAfter(roligPeriode.first) && avgang.isBefore(roligPeriode.last)
    }

    def hentAntallReisendeForAvgang(String avgang, DateTimeFormatter formatter, int varighet) {
        return hentAntallReisendeForAvgang(LocalTime.parse(avgang, formatter).plusMinutes(varighet))
    }

}
