library(ggplot2)

# load FDR and CGD sets
fdr <- read.table("/Users/joeri/github/rvcf/src/test/resources/bundle_r1.0/FDR_allGenes_r1.0.tsv", sep="\t", header=T)
cgd <- read.table("/Users/joeri/github/rvcf/src/test/resources/GenesInheritance31aug2016.tsv", sep="\t", header=T)

# merge, keeping only CGD genes
df <- merge(fdr, cgd, by = "Gene", all.x = TRUE)
df <- df[!is.na(df$Inheritance),]

# replace 0 with 1e-4 to allow log plot and use nice breaks
df$AffectedFrac[df$AffectedFrac == 0] <- 1e-4
df$CarrierFrac[df$CarrierFrac == 0] <- 1e-4
breaks = c(1, 0.1, 0.01, 0.001)

# go go gadget ggplot
ggplot() +
  theme_bw() + theme(panel.grid.major = element_line(colour = "black"), axis.text=element_text(size=16),  axis.title=element_text(size=16,face="bold")) +
  geom_point(data = df, aes(x = AffectedFrac, y = CarrierFrac, shape = Inheritance, colour = Inheritance), size=3, stroke = 3, alpha=0.75) +
  geom_density_2d(data = df, aes(x = AffectedFrac, y = CarrierFrac), colour="Black") +
  geom_text(data = df, aes(x = AffectedFrac, y = CarrierFrac, label = Gene), hjust = 0, nudge_x = 0.01, size = 3, check_overlap = TRUE) +
  ylab("Carrier fraction") +
  xlab("Affected fraction") +
  scale_x_log10(breaks = breaks) +
  scale_y_log10(breaks = breaks)

#save as 12 x 7 inch for good scaling
