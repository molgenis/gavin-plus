library(ggplot2)
setwd("/Users/joeri/github/rvcf/src/test/resources/")

# load HHC and CGD sets
hhc <- read.table("HomHetCounts_r1.0.tsv", sep="\t", header=T)
cgd <- read.table("GenesInheritance31aug2016.tsv", sep="\t", header=T)

# merge, keeping only CGD genes
df <- merge(hhc, cgd, by = "Gene", all.x = TRUE)
levels(df$Inheritance) <- c(levels(df$Inheritance), "UNKNOWN")
df$Inheritance[is.na(df$Inheritance)]<-as.factor("UNKNOWN")

tapply(df$HomFrac, df$Inheritance, median)
tapply(df$HetFrac, df$Inheritance, median)

df <- subset(df, Inheritance == "DOMINANT" | Inheritance == "RECESSIVE")
ggplot() +
  geom_density(data = df, aes(HetFrac, color=Inheritance)) +
  xlim(0,0.01)

ggplot() +
  geom_density(data = df, aes(HomFrac, color=Inheritance)) +
  xlim(0,0.001)


plot(sort(df[df$Inheritance == "RECESSIVE",]$HomFrac), col="red")
points(sort(df[df$Inheritance == "DOMINANT",]$HomFrac))

median(df$HomFrac)*100
mean(df$HetFrac)*100
median(df$HetFrac)*100

df <- df[!is.na(df$Inheritance),]
#levels(df$Inheritance) <- c(levels(df$Inheritance), "UNKNOWN")
#df$Inheritance[is.na(df$Inheritance)]<-as.factor("UNKNOWN")
#df <- subset(df, Inheritance == "UNKNOWN")

# replace 0 with 1e-4 to allow log plot and use nice breaks
df$HomFrac[df$HomFrac == 0] <- 1e-4
df$HetFrac[df$HetFrac == 0] <- 1e-4
breaks = c(1, 0.1, 0.01, 0.001)

# go go gadget ggplot
ggplot() +
  theme_bw() + theme(panel.grid.major = element_line(colour = "black"), axis.text=element_text(size=16),  axis.title=element_text(size=16,face="bold")) +
  geom_point(data = df, aes(x = HomFrac, y = HetFrac, shape = Inheritance, colour = Inheritance), size=1, stroke = 3, alpha=0.75) +
  geom_density_2d(data = df, aes(x = HomFrac, y = HetFrac), colour="Black") +
  geom_text(data = df, aes(x = HomFrac, y = HetFrac, label = Gene), hjust = 0, nudge_x = 0.01, size = 3, check_overlap = TRUE) +
  scale_colour_manual(values=rainbow(7)) +
  scale_shape_manual(values = c(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)) +
  ylab("Heterozygous fraction") +
  xlab("Homozygous fraction") +
  scale_x_log10(breaks = breaks) +
  scale_y_log10(breaks = breaks)

#save as 12 x 7 inch for good scaling
