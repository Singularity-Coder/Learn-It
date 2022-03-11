# InstaScripts
List of useful scripts (python, ruby, shell, etc) for Mobile Developers!

You don’t understand the need of something until you are forced into the painful situation. I finally understand the importance of learning multiple languages and the power and usefulness of regular expressions in python and ruby scripts to quickly do tedious stuff like filtering through logs to debug an issue.

https://stackoverflow.com/questions/5245058/filter-lines-from-a-text-file-which-contain-a-particular-word
Filter lines of a file with a specific Keyword. To execute this command you must be in the same directory as the file that you want to filter: `python findLinesWithKeyword.py` This will execute the script below and generate a file called output with all the lines that contain the keyword mentioned in the "string to search".
```
with open('file_to_search.txt', 'rb') as file_in:
    with open("output.txt", "wb") as file_out:
        file_out.writelines(filter(lambda line: b'string to search' in line, file_in))
```
