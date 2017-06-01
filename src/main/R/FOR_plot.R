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
