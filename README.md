# SIFT4G Annotator

Compilation In Eclipse:

1) Open folder SIFT4G_Annotator_code
2) Compile src/org/siftdna/main/SIFT4G_Main
3) Create Runnable Jar file
4) chmod 775 (at least in linux)

# Annotating a VCF file

To run the SIFT 4G Annotator on Linux or Mac via command line, type the following command into the terminal:
`java -jar <Path to SIFT4G_Annotator> -c -i <Path to input vcf file> -d <Path to SIFT4G database directory> -r <Path to your results folder> -t`

## Options
Option | 	Description
--- | --- 
-c 	| To run on command line
-i 	| Path to your input variants file in VCF format
-d 	| Path to SIFT database directory
-r 	| Path to your output results folder
-t 	| To extract annotations for multiple transcripts (Optional)

## Troubleshooting

If you've built a database following https://github.com/pauline-ng/SIFT4G_Create_Genomic_DB, make sure there are non-empty <chrom>.gz and <chrom>regions files

If your database files start with a 'chr', you must rename them without the 'chr' for the annotator to work.

*Example:* 
`mv chr19.gz -> 19.gz; mv chr19.regions 19.regions; mv chr19_SIFTDB_stats.txt 19_SIFTDB_stats.txt`
