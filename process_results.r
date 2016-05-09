library(devtools)
#install_github("ndphillips/yarrr")
library("yarrr")

load_data <- function(filename){
  files <- list.files(path=filename, pattern="*test-results.csv", full.names=T, recursive=FALSE)
  dataset <- read.table(files[1], header=TRUE, sep=",")
  for (i in 2:length(files)){
    t <- read.table(files[i], header=TRUE, sep=",")
    dataset <- rbind(dataset, t)
  }
  
  dataset['runtime_mins'] = round(dataset['runtime'] / 60000);
  
  dataset['fs'] = dataset['frame_selector']
  
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
  plot(data$runtime_mins, data$lines_covered, type='l', main='Lines', col='white', xlab='Runtime (minutes)', ylab='Lines Covered')
  for (t in types){
    data_type <- data[data$frame_selector == t,]
    d <- aggregate(data_type, by=list(data_type$runtime_mins), FUN=mean, na.rm=TRUE)
    lines((d$runtime/60000), d$lines_covered, type='l', col=colors[counter], lwd=2)
    points((d$runtime/60000), d$lines_covered, col=colors[counter], cex=1, pch=line_types[counter], lwd=3)
    
    counter <- counter + 1
  }
  legend("bottomright", legend=types, col=colors, pch=line_types, lwd=3)
  dev.off()
}


bplot <- function(data, name, title){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.5)
  #boxplot(related_line_coverage~person_method, data=data, main=title, ylab="Line Coverage", las=2, outline=FALSE)
  mi <- round(min(data$related_line_coverage)-0.01, digits=3)
  ma <- round(max(data$related_line_coverage)+0.01, digits=3)
  pirateplot(formula=related_line_coverage~bezier_points + fs, 
             data=data, 
             xlab = 'Training Pool', ylab='Line Coverage', main=title,
             line.fun=median, pal='southpark', ylim=c(mi, ma))
  dev.off()
}
