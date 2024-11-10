import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Stoppested {

    String navn
    Kommune kommune
    List<Avgang> avganger
    double prosentReisende
    Tuple<LocalTime> rushtid = []

    Stoppested(String navn, Kommune kommune, double prosentReisende, Tuple<LocalTime> rushtid = []) {
        this.navn = navn
        this.kommune = kommune
        this.avganger = []
        this.prosentReisende = prosentReisende
        this.rushtid = rushtid
    }

    def leggTilAvganger(Stoppested stoppested, List<LocalTime> avganger) {
        def existingAvgang = this.avganger.find { it.stasjon == stoppested }
        if (existingAvgang) existingAvgang.avganger.addAll(avganger)
        else this.avganger << new Avgang(stoppested, avganger)
    }

    def finnAvganger(Stoppested stoppested) {
        return this.avganger.find { it.stasjon == stoppested }?.avganger ?: []
    }

    def hentAntallReisendeForAvgang(LocalTime avgang) {
        def antallReisende = this.prosentReisende * Kommune.hentInnbyggertall(kommune)
        if (rushtid) {
            if (avgang.isAfter(rushtid.first.minusMinutes(1)) && avgang.isBefore(rushtid.last.plusMinutes(1))) {
                antallReisende = antallReisende * 2
            }
        }
        // TODO this is hardcoded and not how I wanted to do this,
        //  should have some logic around number of stoppesteder within kommuner
        if (this.navn == 'Moelv' || this.navn == 'Brumunddal') {
            antallReisende = antallReisende / 2
        }
        return antallReisende
    }

    def hentAntallReisendeForAvgang(String avgang, DateTimeFormatter formatter) {
        return hentAntallReisendeForAvgang(LocalTime.parse(avgang, formatter))
    }

}
