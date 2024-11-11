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

    int finnReisetid(Stoppested start, Stoppested stopp, Set<Stoppested> besokt = new HashSet<>()) {
        if (start == stopp) return 0
        besokt << start
        for (strekning in stoppesteder[start]) {
            if (!besokt.contains(strekning.stasjon)) {
                int distance = finnReisetid(strekning.stasjon, stopp, besokt)
                if (distance != -1) return strekning.reisetid + distance
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

    // Denne har per nå kun støtte for en retning...
    def hentAntallReisende(String tidspunkt, Stoppested avgangsstasjon, Stoppested endestasjon) {
        def sum = 0
        def gjeldendeStasjon = avgangsstasjon
        while (gjeldendeStasjon && gjeldendeStasjon != endestasjon) {
            sum += gjeldendeStasjon.hentAntallReisendeForAvgang(tidspunkt, formatter)
            gjeldendeStasjon = stoppesteder[gjeldendeStasjon]?.last?.stasjon
        }
        if (gjeldendeStasjon == endestasjon) {
            sum += endestasjon.hentAntallReisendeForAvgang(tidspunkt, formatter)
        }
        return sum
    }

    // Denne har per nå kun støtte for en retning...
    def beregnFortjeneste(String tidspunkt, Stoppested startStasjon) {
        def (sum, multiplikator, besokt) = [0, 1, new HashSet<Stoppested>()]
        def gjeldendeStopp = stoppesteder[startStasjon]?.last
        while (gjeldendeStopp && !besokt.contains(gjeldendeStopp.stasjon)) {
            besokt << gjeldendeStopp.stasjon
            def antallReisende = gjeldendeStopp.stasjon.hentAntallReisendeForAvgang(tidspunkt, formatter)
            sum += (antallReisende * (multiplikator++ * 100))
            gjeldendeStopp = stoppesteder[gjeldendeStopp.stasjon]?.last
        }

        return sum
    }

}
