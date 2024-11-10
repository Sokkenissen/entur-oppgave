import java.time.LocalTime

class Stoppested {

    String navn
    String kommune
    List<Avgang> avganger
    double prosentReisende

    Stoppested(String navn, Kommune kommune, double prosentReisende) {
        this.navn = navn
        this.kommune = kommune
        this.avganger = []
        this.prosentReisende = prosentReisende
    }

    def leggTilAvganger(Stoppested stoppested, List<LocalTime> avganger) {
        def existingAvgang = this.avganger.find { it.stasjon == stoppested }
        if (existingAvgang) existingAvgang.avganger.addAll(avganger)
        else this.avganger << new Avgang(stoppested, avganger)
    }

    def finnAvganger(Stoppested stoppested) {
        return this.avganger.find { it.stasjon == stoppested }?.avganger ?: []
    }

}
