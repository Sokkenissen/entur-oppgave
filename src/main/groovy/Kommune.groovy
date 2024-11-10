enum Kommune {

    LILLEHAMMER("Lillehammer kommune", 30_000, 1),
    RINGSAKER("Ringsaker kommune", 35_000, 2),
    STANGE("Stange kommune", 20_000, 1),
    ULLENSAKER("Ullensaker kommune", 40_000, 1),
    OSLO("Oslo kommune", 700_000, 1)

    String kommunenavn
    int innbyggertall
    int antallStoppesteder

    Kommune(kommunenavn, innbyggertall, antallStoppesteder) {
        this.kommunenavn = kommunenavn
        this.innbyggertall = innbyggertall
        this.antallStoppesteder = antallStoppesteder
    }

    static def hentInnbyggertall(Kommune kommune) {
        return kommune.innbyggertall
    }

    static def antallStoppesteder(Kommune kommune) {
        return kommune.antallStoppesteder
    }

}