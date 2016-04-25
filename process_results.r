library(devtools)
install_github("ndphillips/yarrr")
library("yarrr")

load_data <- function(filename){
  files <- list.files(path=filename, pattern="*test-results.csv", full.names=T, recursive=FALSE)
  dataset <- read.table(files[1], header=TRUE, sep=",")
  for (i in 2:length(files)){
    t <- read.table(files[i], header=TRUE, sep=",")
    dataset <- rbind(dataset, t)
  }
  
  dataset['runtime_mins'] = round(dataset['runtime'] / 60000);
  
  dataset['states'] = dataset['states_found'] + dataset['states_discovered'];
  
  dataset['cluster_identifier'] = apply(dataset, 1, FUN=function(x){
    if (x['frame_selector'] != 'EMPTY'){
      val <- paste(x['runtime_mins'], 'mins', ' ', x['clusters'], 'x', x['ngram'], ' ', sep='')
    } else {
      paste('Empty', ' ', x['runtime_mins'], 'mins', sep='')
      
    }
  })
  
  dataset['person'] = apply(dataset, 1, FUN=function(x){
    if (x['gesture_file'] == 'tom-gorogoa/'){
      val <- 'Person 1'
    } else if (x['gesture_file'] == 'ab-gorogoa/'){
      val <- 'Person 2'
    }  else if (x['gesture_file'] == 'gorogoa/'){
      val <- 'Both'
    } else {
      val <- 'Random'
    }
  })
  
  dataset['person_method'] = apply(dataset, 1, FUN=function(x){
    if (x['gesture_file'] == 'tom-gorogoa/'){
      val <- paste('Person 1[', x['frame_selector'], ']', sep="")
    } else if (x['gesture_file'] == 'ab-gorogoa/'){
      val <- paste('Person 2[', x['frame_selector'], ']', sep="")
    }  else if (x['gesture_file'] == 'gorogoa/'){
      val <- paste('Both[', x['frame_selector'], ']', sep="")
    } else {
      val <- 'Random'
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

line_plot_person <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  types <- (unique(data$person_method))
  types <- sort(types)
  counter <- 1
  #data$line_coverage <- log10(data$line_coverage)
  plot(data$runtime_mins, data$lines_covered, type='l', main='Lines', col='white', xlab='Runtime (minutes)', ylab='Lines Covered')
  for (t in types){
    data_type <- data[data$person_method == t,]
    d <- aggregate(data_type, by=list(data_type$runtime_mins), FUN=mean, na.rm=TRUE)
    lines((d$runtime/60000), d$lines_covered, type='l', col=colors[counter], lwd=2)
    points((d$runtime/60000), d$lines_covered, col=colors[counter], cex=1, pch=line_types[counter], lwd=3)
    
    counter <- counter + 1
  }
  legend("bottomright", legend=types, col=colors, pch=line_types, lwd=3)
  dev.off()
}

line_plot_related <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  types <- (unique(data$frame_selector))
  types <- sort(types)
  counter <- 1
  #data$line_coverage <- log10(data$line_coverage)
  plot(data$runtime_mins, data$related_line_coverage, type='l', main='Related Lines', col='white', xlab='Runtime (minutes)', ylab='Related Line Coverage')
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

line_plot_states <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  types <- (unique(data$frame_selector))
  types <- sort(types)
  counter <- 1
  #data$line_coverage <- log10(data$line_coverage)
  plot(data$runtime_mins, data$states, type='l', main='States', col='white', xlab='Runtime (minutes)', ylab='States Found')
  for (t in types){
    data_type <- data[data$frame_selector == t,]
    d <- aggregate(data_type, by=list(data_type$runtime_mins), FUN=mean, na.rm=TRUE)
    lines((d$runtime/60000), d$states, type='l', col=colors[counter], lwd=2)
    points((d$runtime/60000), d$states, col=colors[counter], cex=1, pch=line_types[counter], lwd=3)
    
    counter <- counter + 1
  }
  legend("bottomright", legend=types, col=colors, pch=line_types, lwd=3)
  dev.off()
}

bplot <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  boxplot(lines_covered~frame_selector, outline=FALSE, data=data, main="Boxplot showing lines covered ", ylab="Lines covered", las=2)
  dev.off()
}

bplot_p <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(line_coverage~person, outline=FALSE, data=data, main="Boxplot showing line coverage across all methods per person", ylab="Lines covered", las=2)
  dev.off()
}

bplot_person_bc <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(relate_branch_coverage~person_method, outline=FALSE, data=data, main="Boxplot showing related branch coverage across all methods per person", ylab="Related Branch Coverage", las=2)
  dev.off()
}

bplot_p_related <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(related_line_coverage~person, outline=FALSE, data=data, main="Boxplot showing average related line coverage across\n all methods per person", ylab="Lines covered", las=2)
  dev.off()
}

bplot_person <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(related_line_coverage~person_method, data=data, main="Boxplot showing lines covered ", ylab="Lines covered", las=2, outline=FALSE)
  dev.off()
}

bplot_person_specific <- function(data, name, person){
  pdf(paste(name, ".pdf", sep=''))
  data <- data[data$person == person,]
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(lines_covered~person_method, data=data, main="Boxplot showing lines covered (10 minutes)", ylab="Lines covered", las=2, outline=FALSE)
  dev.off()
}

bplot_person_related <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(related_line_coverage~person_method, outline=FALSE, data=data, main="Boxplot showing related line coverage ", ylab="Lines covered", las=2)
  dev.off()
}

bplot_states <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1)
  boxplot(states~frame_selector, outline=FALSE, data=data, main="Boxplot showing lines covered ", ylab="Lines covered", las=2)
  dev.off()
}

bplot_states_person <- function(data, name){
  pdf(paste(name, ".pdf", sep=''))
  par(mar = c(10, 5, 5, 4) + 0.1, cex.axis=0.75)
  boxplot(states~person_method, data=data, outline=FALSE, main="Boxplot showing lines covered ", ylab="States Found", las=2)
  dev.off()
}