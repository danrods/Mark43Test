Danny Rodrigues - String API
============================

1. Function 1 : **Average word length**
    For Function 1 I found a list of words assuming that all words are
    delimited by one or more space ' ' characters. After splitting the 
    potential words into tokens I use the regex :
    `\\.|!$|\?$|[a-zA-z]+((-|\'|\")?[a-zA-z]*)*(\?|!|\\.)?`
    Which says that a word is either a period or exclamation point or 
    question mark on it's own. Alternatively it is at least one letter 
    (denoted by the `[a-zA-z]+` followed by any combination of 
    dashes, quotes or ticks subsequently followed by more optional letters to allow
    for Portmanteau words such as 'Happy-Go-Lucky.' Additionally a word
    can end with an ending such as ?,! or . If any numbers are included
    the word will fail to match the regex. With more time and a few tweaks
     this could be perfect, just didn't want to fully follow the rabbit hole,
     to be as efficient as possible.
    
2. Function 2 : **Most common word**
    Function 2, the most common word is easily found by first using a map
    to get the frequency of each word, then iterating over the keyset
    until we've found the one with the highest frequency, keeping track
    of any that have the same frequency in a LinkedList. 
    At the end the list of words with the max frequency is sorted 
    lexicographically and because it is a linked list simply pop the head,
    to the get the first word lexicographically.
    
3. Function 3 : **Median Frequency**
    Function 3, To be succinct as possible the easiest way to find the 
    median is to simply find any words and add them to a list. Sorting
    the list by Frequency and Lexicographically and simply taking the
    middle by indexing half of the length of the array is the simplest
    possible solution. There are more sophisticated ways to do so, but
    this is the most obvious one.
    
4. Function 4 : **Average sentence length**
    Function 4 was quite interesting because it is kind of hard to define
    just exactly what a sentence is and so I had to make a few calls 
    based on what logically makes sense to me and I came up with the
    following regex : `[a-zA-z]+((-|'|")?\s*[a-zA-z]+ |\s*(|;|:|,)\s*[a-zA-z]+)*` 
    This says that a sentence is simply a group of letters(at least one)
    followed by either a -, ' or " followed by a group of letters, at least
    one of them to allow for Happy-Go-Lucky without allowing multiple dashes
    in a row. Alternatively a Sentence can also contain a colon, semicolon
    or commas, followed by any optional amount of spaces, followed by more letters
    to allow for punctuation. Finally, before a sentence can get matched
    using the regex above the text is split into sentences based on the 
    the punctuation ?, ! or . using the regex `(\.|!|\?)` 
    
5. Function 5 : **Find all phone numbers**
    Function 5 was interesting as well, my assumption for this one is
    that the number is always a group of characters in it's own entirety,
    meaning that it is never combined with alpha or other characters, and
    thus there are intertwining of characters besides 0-9 and - or () in
    the phone numbers. In the interest of saving time I've matched only
    numbers up to 10 digits, meaning Phone number + Area Code. International
    Code is not included. Also the numbers allowed can have dashes separating
    the area code and phone number, the area code can have parens, I've
    tested successfully the following combinations : 
    1. (631)-294-6681 
    2. (631)-2946681 
    3. (631)2946681 
    4. 631-294-6681
    5. 631-2946681
    6. 6312946681
    7. 294-6681
    8. 2946681   
    
    The Regex I used for the phone numbers is the following :
    `(\([0-9]{3}\)\-?|[0-9]{3}\-?)?[0-9]{3}\-?[0-9]{4}`
    