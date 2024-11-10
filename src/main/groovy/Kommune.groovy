enum Kommune {

    LILLEHAMMER("Lillehammer kommune", 30_000),
    RINGSAKER("Ringsaker kommune", 35_000),
    STANGE("Stange kommune", 20_000),
    ULLENSAKER("Ullensaker kommune", 40_000),
    OSLO("Oslo kommune", 700_000)

    String kommunenavn
    int innbyggertall

    Kommune(kommunenavn, innbyggertall) {
        this.kommunenavn = kommunenavn
        this.innbyggertall = innbyggertall
    }

    static def hentInnbyggertall(Kommune kommune) {
        return kommune.innbyggertall
    }

}