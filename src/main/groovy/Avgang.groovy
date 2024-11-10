import java.time.LocalTime

class Avgang {

    Stoppested stasjon
    List<LocalTime> avganger

    Avgang(Stoppested stasjon, List<LocalTime> avganger) {
        this.stasjon = stasjon
        this.avganger = avganger
    }
}
