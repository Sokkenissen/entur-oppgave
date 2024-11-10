import java.time.LocalTime

class Stoppested {

    String navn
    String kommune
    Map<Stoppested, List<LocalTime>> avganger

    Stoppested(String navn, Kommune kommune) {
        this.navn = navn
        this.kommune = kommune
        this.avganger = [:]
    }

    def leggTilAvganger(Stoppested stoppested, List<LocalTime> avganger) {
        if (!this.avganger.containsKey(stoppested)) {
            this.avganger[stoppested] = []
        }
        this.avganger[stoppested].addAll(avganger)
    }


}
