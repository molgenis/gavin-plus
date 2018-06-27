library(ggplot2)
setwd("/Users/joeri/github/rvcf/src/test/resources/")

# load FOR and CGD sets
falseOmRate <- read.table("FOR_results_per_gene_r1.0.tsv", sep="\t", header=T)
cgd <- read.table("GenesInheritance31aug2016.tsv", sep="\t", header=T)

# merge, keeping only CGD genes
df <- merge(falseOmRate, cgd, by = "Gene", all.x = TRUE)
df <- df[!is.na(df$Inheritance),]

# go go gadget ggplot
ggplot() +
  theme_bw() + theme(panel.grid.major = element_line(colour = "black"), axis.text=element_text(size=16),  axis.title=element_text(size=16,face="bold")) +
  geom_point(data = df, aes(x = Expected, y = Expected-Observed, shape = Inheritance, colour = Inheritance), size=3, stroke = 3, alpha=0.75) +
  geom_text(data = df, aes(x = Expected, y = Expected-Observed, label = Gene), hjust = 0, nudge_x = 0.01, size = 3, check_overlap = TRUE) +
  ylab("Variants expected-observed (i.e. missed)") +
  xlab("Variants expected (i.e. total per gene)")

ggsave("FOR_plot.pdf", width = 10, height = 6)

##
## update
##

# read old and new FDR data, show means
falseOmRate1.0 <- read.table("FOR_results_per_gene_r1.0.tsv", sep="\t", header=T)
mean(falseOmRate1.0$MissedFrac)
falseOmRate1.2 <- read.table("FOR_results_per_gene_r1.2.tsv", sep="\t", header=T)
mean(falseOmRate1.2$MissedFrac)

# calculate missed and plot for inspection
falseOmRate1.2$missed <- falseOmRate1.2$Expected-falseOmRate1.2$Observed
falseOmRate1.0$missed <- falseOmRate1.0$Expected-falseOmRate1.0$Observed
plot(falseOmRate1.0$missed ~ falseOmRate1.2$missed)

# merge, keeping only CGD genes
mfor <- merge(falseOmRate1.0, falseOmRate1.2, by="Gene")
cgd <- read.table("GenesInheritance26jun2018.tsv", sep="\t", header=T)
mforCGD <- merge(mfor, cgd, by = "Gene", all.x = TRUE)
mforCGD <- mforCGD[!is.na(mforCGD$Inheritance),]

# go go gadget ggplot to show differences nicely
ggplot() +
  theme_bw() + theme(panel.grid = element_blank()) +
  geom_abline(colour="gray", size=5) +
  geom_point(data = mforCGD, aes(x = missed.x, y = missed.y, shape = Inheritance, colour = Inheritance), size=3, stroke = 3, alpha=0.75) +
  geom_text(data = mforCGD, aes(x = missed.x, y = missed.y, label = Gene), hjust = 0, nudge_x = 0.01, size = 3, check_overlap = TRUE) +
  ylab("Variants missed in r1.2") +
  xlab("Variants missed in r1.0")

ggsave("FOR_comp_plot.pdf", width = 10, height = 6)
