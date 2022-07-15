-------------------------------- MODULE part1aTLAspec --------------------------------

EXTENDS TLC, Integers, Sequences, FiniteSets

FilesMockup == {"DivinaCommedia.pdf", "PromessiSposi.pdf", "AChrismasCarol.pdf"}
documentsContentMockup == {
    "Nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura, che la diritta via era smarrita",
    "Quel ramo del lago di Como, che volge a mezzogiorno, tra due catene non interrotte di monti, tutto a seni e a golfi",
    "Marley was dead: to begin with. There is no doubt whatever about that. The register of his burial was signed by the clergyman, the clerk, the undertaker, and the chief mourner."
}
Loaders == {"loader-thread-1", "loader-thread-2"}
Analyzers == {"analyzer-thread-1", "analyzer-thread-2"}

(* --algorithm part1a_architecture

variables 
    docDiscovered = <<>>,          \* buffer per i file pdf
    docLoaded = <<>>,              \* buffer per i documenti caricati in RAM

    DiscovererCanStart = FALSE,    \* used for synchronization purposes
    docDiscoveredClosed = FALSE,
    docLoadedClosed = FALSE,

    nLoadersDone = 0,              \* latch for Loaders
    nAnalyzersDone = 0;            \* latch for analyzers


fair+ process Master = "master-thread"
begin
    Coordination:
        DiscovererCanStart := TRUE;
    SynchWithLoaders:
        await nLoadersDone = Cardinality(Loaders);
    ClosingBuffer:
        docLoadedClosed := TRUE;
    SynchWithAnalyzers:
        await nAnalyzersDone = Cardinality(Analyzers);
    Exiting:
        print "master-thread";
        print "done";
        print "------------------------------------";
end process;


fair+ process Discoverer = "discoverer-thread"
begin
    SynchWithMaster:
        await DiscovererCanStart;
    Discovering:
        with file \in FilesMockup do
            docDiscovered := Append(docDiscovered, file);
        end with;
    ClosingBuffer:
        docDiscoveredClosed := TRUE;
    Exiting:
        print "discoverer-thread";
        print "done";
        print "------------------------------------";
end process;


fair+ process Loader \in Loaders
variable item = "none"
begin
    Work:
        while docDiscovered /= <<>> \/ (docDiscovered = <<>> /\ ~docDiscoveredClosed) do
            await docDiscovered /= <<>> \/ docDiscoveredClosed;

            GettingFilesToLoad:
                if docDiscovered /= <<>> then
                    item := Head(docDiscovered);
                    docDiscovered := Tail(docDiscovered);

                    ProcessingItem:
                        print self;
                        print item;
                        print "------------------------------------";
            
                    LoadingFiles:
                        with docContent \in documentsContentMockup do
                            docLoaded := Append(docLoaded, docContent);
                        end with;
                else
                    skip
                end if;
        end while;
    Latch:
        nLoadersDone := nLoadersDone + 1;
    Exiting:
        print self;
        print "done";
        print "------------------------------------";
end process;


fair+ process Analyzer \in Analyzers
variable item = "none"
begin
    Work:
        while docLoaded /= <<>> \/ (docLoaded = <<>> /\ ~docLoadedClosed) do
            await docLoaded /= <<>> \/ docLoadedClosed;

            GettingContentToAnalyze:
                if docLoaded /= <<>> then
                    item := Head(docLoaded);
                    docLoaded := Tail(docLoaded);

                    ProcessingItem:
                        print self;
                        print item;
                        print "------------------------------------";
                else
                    skip
                end if;
        end while;
    Latch:
        nAnalyzersDone := nAnalyzersDone + 1;
    Exiting:
        print self;
        print "done";
        print "------------------------------------";
end process;

end algorithm *)

====
