import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Rute {

    static private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm")

    String rutenavn
    LinkedHashMap<Stoppested, ArrayList<Strekning>> stoppesteder

    Rute(String rutenavn) {
        this.rutenavn = rutenavn
        stoppesteder = new LinkedList<>()
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

    def hentAvganger(String tidspunkt, Stoppested stoppested) {
        // TODO handle illegal input
        def fraTid = LocalTime.parse(tidspunkt, formatter)
        def stopp = stoppesteder.find { it.key == stoppested }.key
        def avganger = []
        if (stopp) {
            stopp.avganger.each { k, v ->
                // Use builtin LocalTime.isBefore to fetch all departures before a given time
                avganger << v.findAll { it.isBefore(fraTid) }
            }
        }
        return avganger.flatten() // avganger is a 2 dimensional list, flatten it
    }

    @Override
    String toString() {
        stoppesteder.collect { k, v -> "${k.navn} -> ${v.last().tilknyttede.navn}" }.join("\n")
    }

}
