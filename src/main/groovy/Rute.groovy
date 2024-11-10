import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Rute {

    static private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm")

    String rutenavn
    LinkedHashMap<Stoppested, ArrayList<Strekning>> stoppesteder

    Rute(String rutenavn) {
        this.rutenavn = rutenavn
        stoppesteder = new LinkedHashMap<>()
    }

    def leggTilStoppested(Stoppested a, Stoppested b, int reisetid) {
        stoppesteder.putIfAbsent(a,[])
        stoppesteder.putIfAbsent(b,[])
        stoppesteder[a] << new Strekning(b, reisetid)
        stoppesteder[b] << new Strekning(a, reisetid)
    }

    // TODO rydd opp i denne
    int finnReisetid(Stoppested start, Stoppested stopp, Set<Stoppested> visited = new HashSet<>()) {
        if (start == stopp) return 0

        visited << start

        for (edge in stoppesteder[start]) {
            if (!visited.contains(edge.tilknyttede)) {
                int distance = finnReisetid(edge.tilknyttede, stopp, visited)
                if (distance != -1) return edge.reisetid + distance
            }
        }
        return -1
    }

    List<Avgang> hentAvganger(String tidspunkt, Stoppested stoppested, boolean brukEksaktTid = false) {
        // TODO handle illegal input
        def fraTid = LocalTime.parse(tidspunkt, formatter)
        def stopp = stoppesteder.find { it.key == stoppested }.key
        def avganger = []
        if (stopp) {
            stopp.avganger.each { avgang ->
                def matchingTimes = brukEksaktTid
                        ? avgang.avganger.findAll { it == fraTid }
                        : avgang.avganger.findAll { it.isBefore(fraTid) }
                // This is supid... But works for now
                // TODO refactor
                if (matchingTimes) avganger.add(new Avgang(avgang.stasjon, matchingTimes))
            }
        }
        return avganger
    }

    def hentAntallReisende(String tidspunkt, Stoppested avgangsstasjon) {
        def avgang = hentAvganger(tidspunkt, avgangsstasjon, true)
        if (avgang) {
            return 1
        }
        return -1
    }

    @Override
    String toString() {
        stoppesteder.collect { k, v -> "${k.navn} -> ${v.last().tilknyttede.navn}" }.join("\n")
    }

}
