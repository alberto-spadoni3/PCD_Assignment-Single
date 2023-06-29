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
    SyncWithLoaders:
        await nLoadersDone = Cardinality(Loaders);
    ClosingBuffer:
        docLoadedClosed := TRUE;
    SyncWithAnalyzers:
        await nAnalyzersDone = Cardinality(Analyzers);
    Exiting:
        print "master-thread";
        print "done";
        print "------------------------------------";
end process;


fair+ process Discoverer = "discoverer-thread"
begin
    SyncWithMaster:
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
            when docDiscovered /= <<>> \/ docDiscoveredClosed;

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
            when docLoaded /= <<>> \/ docLoadedClosed;

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
\* BEGIN TRANSLATION (chksum(pcal) = "1c5fbd60" /\ chksum(tla) = "5195b320")
\* Label ClosingBuffer of process Master at line 35 col 9 changed to ClosingBuffer_
\* Label Exiting of process Master at line 39 col 9 changed to Exiting_
\* Label Exiting of process Discoverer at line 56 col 9 changed to Exiting_D
\* Label Work of process Loader at line 66 col 9 changed to Work_
\* Label ProcessingItem of process Loader at line 75 col 25 changed to ProcessingItem_
\* Label Latch of process Loader at line 88 col 9 changed to Latch_
\* Label Exiting of process Loader at line 90 col 9 changed to Exiting_L
\* Process variable item of process Loader at line 63 col 10 changed to item_
VARIABLES docDiscovered, docLoaded, DiscovererCanStart, docDiscoveredClosed, 
          docLoadedClosed, nLoadersDone, nAnalyzersDone, pc, item_, item

vars == << docDiscovered, docLoaded, DiscovererCanStart, docDiscoveredClosed, 
           docLoadedClosed, nLoadersDone, nAnalyzersDone, pc, item_, item >>

ProcSet == {"master-thread"} \cup {"discoverer-thread"} \cup (Loaders) \cup (Analyzers)

Init == (* Global variables *)
        /\ docDiscovered = <<>>
        /\ docLoaded = <<>>
        /\ DiscovererCanStart = FALSE
        /\ docDiscoveredClosed = FALSE
        /\ docLoadedClosed = FALSE
        /\ nLoadersDone = 0
        /\ nAnalyzersDone = 0
        (* Process Loader *)
        /\ item_ = [self \in Loaders |-> "none"]
        (* Process Analyzer *)
        /\ item = [self \in Analyzers |-> "none"]
        /\ pc = [self \in ProcSet |-> CASE self = "master-thread" -> "Coordination"
                                        [] self = "discoverer-thread" -> "SyncWithMaster"
                                        [] self \in Loaders -> "Work_"
                                        [] self \in Analyzers -> "Work"]

Coordination == /\ pc["master-thread"] = "Coordination"
                /\ DiscovererCanStart' = TRUE
                /\ pc' = [pc EXCEPT !["master-thread"] = "SyncWithLoaders"]
                /\ UNCHANGED << docDiscovered, docLoaded, docDiscoveredClosed, 
                                docLoadedClosed, nLoadersDone, nAnalyzersDone, 
                                item_, item >>

SyncWithLoaders == /\ pc["master-thread"] = "SyncWithLoaders"
                   /\ nLoadersDone = Cardinality(Loaders)
                   /\ pc' = [pc EXCEPT !["master-thread"] = "ClosingBuffer_"]
                   /\ UNCHANGED << docDiscovered, docLoaded, 
                                   DiscovererCanStart, docDiscoveredClosed, 
                                   docLoadedClosed, nLoadersDone, 
                                   nAnalyzersDone, item_, item >>

ClosingBuffer_ == /\ pc["master-thread"] = "ClosingBuffer_"
                  /\ docLoadedClosed' = TRUE
                  /\ pc' = [pc EXCEPT !["master-thread"] = "SyncWithAnalyzers"]
                  /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                                  docDiscoveredClosed, nLoadersDone, 
                                  nAnalyzersDone, item_, item >>

SyncWithAnalyzers == /\ pc["master-thread"] = "SyncWithAnalyzers"
                     /\ nAnalyzersDone = Cardinality(Analyzers)
                     /\ pc' = [pc EXCEPT !["master-thread"] = "Exiting_"]
                     /\ UNCHANGED << docDiscovered, docLoaded, 
                                     DiscovererCanStart, docDiscoveredClosed, 
                                     docLoadedClosed, nLoadersDone, 
                                     nAnalyzersDone, item_, item >>

Exiting_ == /\ pc["master-thread"] = "Exiting_"
            /\ PrintT("master-thread")
            /\ PrintT("done")
            /\ PrintT("------------------------------------")
            /\ pc' = [pc EXCEPT !["master-thread"] = "Done"]
            /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                            docDiscoveredClosed, docLoadedClosed, nLoadersDone, 
                            nAnalyzersDone, item_, item >>

Master == Coordination \/ SyncWithLoaders \/ ClosingBuffer_
             \/ SyncWithAnalyzers \/ Exiting_

SyncWithMaster == /\ pc["discoverer-thread"] = "SyncWithMaster"
                  /\ DiscovererCanStart
                  /\ pc' = [pc EXCEPT !["discoverer-thread"] = "Discovering"]
                  /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                                  docDiscoveredClosed, docLoadedClosed, 
                                  nLoadersDone, nAnalyzersDone, item_, item >>

Discovering == /\ pc["discoverer-thread"] = "Discovering"
               /\ \E file \in FilesMockup:
                    docDiscovered' = Append(docDiscovered, file)
               /\ pc' = [pc EXCEPT !["discoverer-thread"] = "ClosingBuffer"]
               /\ UNCHANGED << docLoaded, DiscovererCanStart, 
                               docDiscoveredClosed, docLoadedClosed, 
                               nLoadersDone, nAnalyzersDone, item_, item >>

ClosingBuffer == /\ pc["discoverer-thread"] = "ClosingBuffer"
                 /\ docDiscoveredClosed' = TRUE
                 /\ pc' = [pc EXCEPT !["discoverer-thread"] = "Exiting_D"]
                 /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                                 docLoadedClosed, nLoadersDone, nAnalyzersDone, 
                                 item_, item >>

Exiting_D == /\ pc["discoverer-thread"] = "Exiting_D"
             /\ PrintT("discoverer-thread")
             /\ PrintT("done")
             /\ PrintT("------------------------------------")
             /\ pc' = [pc EXCEPT !["discoverer-thread"] = "Done"]
             /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                             docDiscoveredClosed, docLoadedClosed, 
                             nLoadersDone, nAnalyzersDone, item_, item >>

Discoverer == SyncWithMaster \/ Discovering \/ ClosingBuffer \/ Exiting_D

Work_(self) == /\ pc[self] = "Work_"
               /\ IF docDiscovered /= <<>> \/ (docDiscovered = <<>> /\ ~docDiscoveredClosed)
                     THEN /\ docDiscovered /= <<>> \/ docDiscoveredClosed
                          /\ pc' = [pc EXCEPT ![self] = "GettingFilesToLoad"]
                     ELSE /\ pc' = [pc EXCEPT ![self] = "Latch_"]
               /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                               docDiscoveredClosed, docLoadedClosed, 
                               nLoadersDone, nAnalyzersDone, item_, item >>

GettingFilesToLoad(self) == /\ pc[self] = "GettingFilesToLoad"
                            /\ IF docDiscovered /= <<>>
                                  THEN /\ item_' = [item_ EXCEPT ![self] = Head(docDiscovered)]
                                       /\ docDiscovered' = Tail(docDiscovered)
                                       /\ pc' = [pc EXCEPT ![self] = "ProcessingItem_"]
                                  ELSE /\ TRUE
                                       /\ pc' = [pc EXCEPT ![self] = "Work_"]
                                       /\ UNCHANGED << docDiscovered, item_ >>
                            /\ UNCHANGED << docLoaded, DiscovererCanStart, 
                                            docDiscoveredClosed, 
                                            docLoadedClosed, nLoadersDone, 
                                            nAnalyzersDone, item >>

ProcessingItem_(self) == /\ pc[self] = "ProcessingItem_"
                         /\ PrintT(self)
                         /\ PrintT(item_[self])
                         /\ PrintT("------------------------------------")
                         /\ pc' = [pc EXCEPT ![self] = "LoadingFiles"]
                         /\ UNCHANGED << docDiscovered, docLoaded, 
                                         DiscovererCanStart, 
                                         docDiscoveredClosed, docLoadedClosed, 
                                         nLoadersDone, nAnalyzersDone, item_, 
                                         item >>

LoadingFiles(self) == /\ pc[self] = "LoadingFiles"
                      /\ \E docContent \in documentsContentMockup:
                           docLoaded' = Append(docLoaded, docContent)
                      /\ pc' = [pc EXCEPT ![self] = "Work_"]
                      /\ UNCHANGED << docDiscovered, DiscovererCanStart, 
                                      docDiscoveredClosed, docLoadedClosed, 
                                      nLoadersDone, nAnalyzersDone, item_, 
                                      item >>

Latch_(self) == /\ pc[self] = "Latch_"
                /\ nLoadersDone' = nLoadersDone + 1
                /\ pc' = [pc EXCEPT ![self] = "Exiting_L"]
                /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                                docDiscoveredClosed, docLoadedClosed, 
                                nAnalyzersDone, item_, item >>

Exiting_L(self) == /\ pc[self] = "Exiting_L"
                   /\ PrintT(self)
                   /\ PrintT("done")
                   /\ PrintT("------------------------------------")
                   /\ pc' = [pc EXCEPT ![self] = "Done"]
                   /\ UNCHANGED << docDiscovered, docLoaded, 
                                   DiscovererCanStart, docDiscoveredClosed, 
                                   docLoadedClosed, nLoadersDone, 
                                   nAnalyzersDone, item_, item >>

Loader(self) == Work_(self) \/ GettingFilesToLoad(self)
                   \/ ProcessingItem_(self) \/ LoadingFiles(self)
                   \/ Latch_(self) \/ Exiting_L(self)

Work(self) == /\ pc[self] = "Work"
              /\ IF docLoaded /= <<>> \/ (docLoaded = <<>> /\ ~docLoadedClosed)
                    THEN /\ docLoaded /= <<>> \/ docLoadedClosed
                         /\ pc' = [pc EXCEPT ![self] = "GettingContentToAnalyze"]
                    ELSE /\ pc' = [pc EXCEPT ![self] = "Latch"]
              /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                              docDiscoveredClosed, docLoadedClosed, 
                              nLoadersDone, nAnalyzersDone, item_, item >>

GettingContentToAnalyze(self) == /\ pc[self] = "GettingContentToAnalyze"
                                 /\ IF docLoaded /= <<>>
                                       THEN /\ item' = [item EXCEPT ![self] = Head(docLoaded)]
                                            /\ docLoaded' = Tail(docLoaded)
                                            /\ pc' = [pc EXCEPT ![self] = "ProcessingItem"]
                                       ELSE /\ TRUE
                                            /\ pc' = [pc EXCEPT ![self] = "Work"]
                                            /\ UNCHANGED << docLoaded, item >>
                                 /\ UNCHANGED << docDiscovered, 
                                                 DiscovererCanStart, 
                                                 docDiscoveredClosed, 
                                                 docLoadedClosed, nLoadersDone, 
                                                 nAnalyzersDone, item_ >>

ProcessingItem(self) == /\ pc[self] = "ProcessingItem"
                        /\ PrintT(self)
                        /\ PrintT(item[self])
                        /\ PrintT("------------------------------------")
                        /\ pc' = [pc EXCEPT ![self] = "Work"]
                        /\ UNCHANGED << docDiscovered, docLoaded, 
                                        DiscovererCanStart, 
                                        docDiscoveredClosed, docLoadedClosed, 
                                        nLoadersDone, nAnalyzersDone, item_, 
                                        item >>

Latch(self) == /\ pc[self] = "Latch"
               /\ nAnalyzersDone' = nAnalyzersDone + 1
               /\ pc' = [pc EXCEPT ![self] = "Exiting"]
               /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                               docDiscoveredClosed, docLoadedClosed, 
                               nLoadersDone, item_, item >>

Exiting(self) == /\ pc[self] = "Exiting"
                 /\ PrintT(self)
                 /\ PrintT("done")
                 /\ PrintT("------------------------------------")
                 /\ pc' = [pc EXCEPT ![self] = "Done"]
                 /\ UNCHANGED << docDiscovered, docLoaded, DiscovererCanStart, 
                                 docDiscoveredClosed, docLoadedClosed, 
                                 nLoadersDone, nAnalyzersDone, item_, item >>

Analyzer(self) == Work(self) \/ GettingContentToAnalyze(self)
                     \/ ProcessingItem(self) \/ Latch(self)
                     \/ Exiting(self)

(* Allow infinite stuttering to prevent deadlock on termination. *)
Terminating == /\ \A self \in ProcSet: pc[self] = "Done"
               /\ UNCHANGED vars

Next == Master \/ Discoverer
           \/ (\E self \in Loaders: Loader(self))
           \/ (\E self \in Analyzers: Analyzer(self))
           \/ Terminating

Spec == /\ Init /\ [][Next]_vars
        /\ SF_vars(Master)
        /\ SF_vars(Discoverer)
        /\ \A self \in Loaders : SF_vars(Loader(self))
        /\ \A self \in Analyzers : SF_vars(Analyzer(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION 

======================================================================================
