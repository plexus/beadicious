(defproject beadicious "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Mozilla Public License Version 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]]

  :resource-paths ["beads-jars/org-jaudiolibs-audioservers.jar"
                   "beads-jars/jl1.0.1.jar"
                   "beads-jars/org-jaudiolibs-audioservers-jack.jar"
                   "beads-jars/beads.jar"
                   "beads-jars/jna.jar"
                   "beads-jars/tritonus_share.jar"
                   "beads-jars/org-jaudiolibs-audioservers-javasound.jar"
                   "beads-jars/tools.jar"
                   "beads-jars/beads-io.jar"
                   "beads-jars/jarjar-1.0.jar"
                   "beads-jars/tritonus_aos-0.3.6.jar"
                   "beads-jars/mp3spi1.9.4.jar"
                   "beads-jars/org-jaudiolibs-jnajack.jar"])
