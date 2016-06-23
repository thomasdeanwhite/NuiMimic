library(devtools)
#install_github("ndphillips/yarrr")
library("yarrr")

load_positions <- function(filename, file_pattern){
  files <- list.files(path=filename, pattern=paste(file_pattern, "*", sep=""), full.names=T, recursive=FALSE)
  dataset <- read.table(files[1], header=FALSE, sep=",")
  for (i in 2:length(files)){
    t <- read.table(files[i], header=FALSE, sep=",")
    dataset <- rbind(dataset, t)
  }
  
  return(table(dataset[1]))
}

load_data <- function(filename){
  files <- list.files(path=filename, pattern="*test-results.csv", full.names=T, recursive=FALSE)
  dataset <- read.table(files[1], header=TRUE, sep=",")
  for (i in 2:length(files)){
    t <- read.table(files[i], header=TRUE, sep=",")
    dataset <- rbind(dataset, t)
  }
  
  replicate <- dataset[dataset$switch_time == 1,]
  
  types <- (unique(dataset$switch_time[dataset$switch_time != 1]))
  
  #for (t in types){
  #  r <- replicate
  #  r$switch_time <- t
  #  dataset <- rbind(r, dataset)
  #}
  
  dataset <- dataset[!dataset$switch_time == 1,]
  
  dataset['runtime_mins'] = round(dataset['runtime'] / 60000);
  
  dataset['fs'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] == 'RANDOM'){
      val <- 'RNG'
    } else {
      val <- x['frame_selector']
    }
  })
  
  dataset['bp'] = dataset['bezier_points']
  
  dataset['states'] = dataset['states_found'] + dataset['states_discovered'];
  
  dataset['cluster_identifier'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] != 'EMPTY'){
      val <- paste(x['runtime_mins'], 'mins', ' ', x['clusters'], 'x', x['ngram'], ' ', sep='')
    } else {
      paste('Empty', ' ', x['runtime_mins'], 'mins', sep='')
      
    }
  })
  
  dataset['person'] = apply(dataset, 1, FUN=function(x){
    if (x['gesture_files'] == 'tom-gorogoa;'){
      val <- 'P1'
    } else if (x['gesture_files'] == 'ab-gorogoa;'){
      val <- 'P2'
    } else if (x['gesture_files'] == 'gorogoa;'){
      val <- 'Both'
    } else {
      val <- 'Random'
    }
  })
  
  dataset['person_method'] = apply(dataset, 1, FUN=function(x){
    if (x['person'] == 'Random'){
      val <- 'Random'
    } else {
      val <- paste(x['person'], '[', x['frame_selector'], ']', sep='')
    }
  })
  
  dataset['cluster_training_identifier'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] != 'EMPTY'){
      val <- paste(x['last_file'], ' ', x['clusters'], 'x', x['ngram'], ' ', sep='')
    } else {
      return('Empty')
      
    }
  })
  
  dataset <- dataset[complete.cases(dataset),]
  
  dataset['td_identifier'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] != 'EMPTY'){
      val <- paste(x['last_file'], x['runtime_mins'], 'mins', ' ', sep='')
    } else {
      paste(x['last_file'], ' ', x['runtime_mins'], 'mins', sep='')
      
    }
  });
  
  dataset['time_identifier'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] != 'EMPTY'){
      return(x['last_file'])
    } else {
      return('Empty')
      
    }
  });
  
  return(dataset)
}

colors <- c('blue', 'red', 'green', 'orange', 'yellow', 'cyan', 'purple', 'pink', 'brown', 'grey')
line_types = c(18, 17, 15, 16, 19, 20, 21, 22, 23, 24)

line_plot <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  types <- (unique(data$frame_selector))
  types <- sort(types)
  counter <- 1
  #data$line_coverage <- log10(data$line_coverage)
  plot(data$runtime_mins, data$related_line_coverage, type='l', main='Lines', col='white', xlab='Runtime (minutes)', ylab='Lines Covered')
  for (t in types){
    data_type <- data[data$frame_selector == t,]
    d <- aggregate(data_type, by=list(data_type$runtime_mins), FUN=mean, na.rm=TRUE)
    lines((d$runtime/60000), d$related_line_coverage, type='l', col=colors[counter], lwd=2)
    points((d$runtime/60000), d$related_line_coverage, col=colors[counter], cex=1, pch=line_types[counter], lwd=3)
    
    counter <- counter + 1
  }
  legend("bottomright", legend=types, col=colors, pch=line_types, lwd=3)
  dev.off()
}

bplot <- function(data, name, title){
  pdf(paste(name, ".pdf", sep=''), height=8.27, width=11.69)
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=1)
  #boxplot(related_line_coverage~person_method, data=data, main=title, ylab="Line Coverage", las=2, outline=FALSE)
  mi <- round(min(data$related_line_coverage)-0.001, digits=3)
  ma <- round(max(data$related_line_coverage)+0.001, digits=3)
  
  data$v = data$fs#paste(data$fs, data$switch_time, sep="")
  
  data <- data[order(data$v, data$bp),]
  
  boxplot(formula=related_line_coverage~v, 
          data=data, 
          xlab = 'Training Pool', ylab='Related Line Coverage', main=title, ylim=c(mi, ma))
  dev.off()
}

bplot_filter <- function(data, name, title, filter){
  
  data <- data[data$frame_selector %in% filter,]
  
  bplot(data, name, title)
}

pplot <- function(data, name, title){
  pdf(paste(name, ".pdf", sep=''), height=8.27, width=11.69)
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.5)
  #boxplot(related_line_coverage~person_method, data=data, main=title, ylab="Line Coverage", las=2, outline=FALSE)
  mi <- round(min(data$related_line_coverage)-0.001, digits=3)
  ma <- round(max(data$related_line_coverage)+0.001, digits=3)
  
  data$v = data$fs#paste(data$fs, data$switch_time, sep="")
  
  data <- data[order(data$v, data$bp),]
  
  pirateplot(formula=related_line_coverage~fs, 
             data=data, 
             xlab = 'Training Pool', ylab='Related Line Coverage', main=title,
             line.fun=median, ylim=c(mi, ma))
  ticks<-c(0.01,0.02,0.03,0.04)
  axis(2,at=ticks,labels=ticks)
  dev.off()
}

pplot_filter <- function(data, name, title, filter){
  
  data <- data[data$frame_selector %in% filter,]
  
  pplot(data, name, title)
}
