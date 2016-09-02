library(ggplot2)

# load FDR and CGD sets
fdr <- read.table("/Users/joeri/github/rvcf/src/test/resources/bundle_r0.1/FDR_allGenes.tsv", sep="\t", header=T)
cgd <- read.table("/Users/joeri/github/rvcf/src/test/resources/GenesInheritance.tsv", sep="\t", header=T)

# merge, keeping only CGD genes
df <- merge(fdr, cgd, by = "Gene", all.x = TRUE)
df <- df[!is.na(df$Inheritance),]

# replace 0 with 1e-4 to allow log plot and use nice breaks
df$AffectedPerc[df$AffectedPerc == 0] <- 1e-4
df$CarrierPerc[df$CarrierPerc == 0] <- 1e-4
breaks = c(1, 0.1, 0.01, 0.001)

# go go gadget ggplot
ggplot() +
  theme_bw() + theme(panel.grid.major = element_line(colour = "black"), axis.text=element_text(size=16),  axis.title=element_text(size=16,face="bold")) +
  geom_point(data = df, aes(x = AffectedPerc, y = CarrierPerc, shape = Inheritance, colour = Inheritance), size=3, stroke = 3, alpha=0.8) +
  geom_density_2d(data = df, aes(x = AffectedPerc, y = CarrierPerc), colour="Black") +
  geom_text(data = df, aes(x = AffectedPerc, y = CarrierPerc, label = Gene), hjust = 0, nudge_x = 0.01, size = 3, check_overlap = TRUE) +
  ylab("Carrier fraction") +
  xlab("Affected fraction") +
  scale_x_log10(breaks = breaks) +
  scale_y_log10(breaks = breaks)

