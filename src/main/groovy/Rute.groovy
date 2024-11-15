import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

    List<Avgang> hentAvganger(String tidspunkt, Stoppested stoppested) {
        def parsedTidspunkt = {
            try {
                LocalTime.parse(tidspunkt, formatter)
            } catch (DateTimeParseException ex) {
                println "Kunne ikke parse $tidspunkt som gyldig tid"
                throw ex
            }
        }
        return hentAvganger(parsedTidspunkt(), stoppested)
    }

    List<Avgang> hentAvganger(LocalTime tidspunkt, Stoppested stoppested) {
        def stopp = stoppesteder.find { it.key == stoppested }?.key
        return stopp ? stopp.avganger.findAll {
            it.avganger.any { it.isBefore(tidspunkt) }
        }.collect {
            new Avgang(it.stasjon, it.avganger.findAll {it.isBefore(tidspunkt) })
        } : []
    }

    // Denne har per nå kun støtte for en retning...
    def hentAntallReisende(String tidspunkt, Stoppested avgangsstasjon, Stoppested endestasjon) {
        def (gjeldendeStasjon, sum, varighet) = [avgangsstasjon, 0, 0]
        while (gjeldendeStasjon && gjeldendeStasjon != endestasjon) {
            sum += gjeldendeStasjon.hentAntallReisendeForAvgang(tidspunkt, formatter, varighet)
            gjeldendeStasjon = stoppesteder[gjeldendeStasjon]?.last?.stasjon
            varighet += stoppesteder[gjeldendeStasjon]?.reisetid.first
        }
        return sum
    }

    // Denne har per nå kun støtte for en retning...
    def beregnFortjeneste(String tidspunkt, Stoppested start, Stoppested stopp = null) {
        def (gjeldendeStasjon, sum, varighet, multiplukator) = [start, 0, 0, 6]
        def endestasjon = stopp == null ? stoppesteder.lastEntry().key : stopp
        while (gjeldendeStasjon && gjeldendeStasjon != endestasjon) {
            def antallReisende = gjeldendeStasjon.hentAntallReisendeForAvgang(tidspunkt, formatter, varighet)
            sum += antallReisende * 100 * multiplukator--
            gjeldendeStasjon = stoppesteder[gjeldendeStasjon]?.last?.stasjon
            varighet += stoppesteder[gjeldendeStasjon]?.reisetid?.first
        }
        return sum
    }

}
