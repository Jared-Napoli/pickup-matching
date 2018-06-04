## Instructions to run:
<p>
All code for program is in src/ directory so...
</p>

1. Download from github
2. cd into project directory
3. On the command line run: _kotlinc src -include-runtime -d PickupMatching.jar_
4. Then run: _kotlin PickupMatching.jar_


# Documentation
## Initial Thoughts

<p>
After my first read through of the assignment, my initial feeling was that it was very doable, but I could see a few challenging aspects. Timezones are known to be tricky and the packed bit system used for the food categories and the recipient hours was a clever storage saving trick but I knew that would make for a more complicated job  of using that information. The information on the .csv files gave a pretty strong hint as to how some of my classes/objects would be organized. It was immediately clear I wouldn't be needing all of the information on them to solve this problem, but I hadn't decided whether I would store that information for use in my output this early on. 
</p>

## Approach/Plan
<p>
My first step to solving any problem is attempting to understand the problem. I reread the instructions several times and slowly pieced together a mental rough draft for what I would be doing. This process includes determining a loose idea of what my class design will be, storage constraints, and runtime efficiency will be. 
</p>
<p>
After I had a general mental blueprint, I needed to decide what technologies I would use. Java, being my most comfortable language, was my first thought but knowing you guys use Kotlin for your backend made this seem like a good opportunity to start learning that language. This meant that I spent the first day and a half or so learning Kotlin since I have never used it before. This set me a little further behind and I knew it would initially slow my development speed but I felt it would be worth it for the experience.
</p>
<p>
Once I was relatively comfortable with Kotlin I started working on the project using my basic understanding of the problem from earlier. The project setup was a bit of a rocky start, I initially tried running a very simplified Kotlin program through the command line but had trouble getting JUnit(the most popular testing framework based on a bit of research) testing to work. I switched to using IntelliJ IDEA because of the ease of testing and once I figured out the quirks of that IDE I was ready to start coding the assignment.
</p>
<p>
My rough outline for the assignment had an O(N^2) runtime and I planned on storing only the required columns from the .csv files into memory as data classes representing the Customers and Recipients. Whether or not I would change the data classes into full-fledged classes was something I would think about throughout my implementation. My main class, PickupMatching, would handle interfacing the two data classes together and would handle all of the .csv input and output.
</p>
<p>
I broke the actual coding of the assignment down into several tasks and decided each would be a branch on Github. In order, the tasks were:
</p>

1. Read input from Recipients.csv
2. Store as a list of Recipients
3. Read input from Customers.csv and store as Customer
4. As one customer, iterate over all Recipients and determine distance
5. Determine count of food matches
6. Determine recipient availability
7. Get scoring working
8. Output to file
9. Refactoring
10. Add more testing

<p>
Working at it this way helped me chip away at the problem; it kept me organized and it gave me an easy way to separate my functions. The actual coding of the assignment went fairly smoothly. Along the way I made adjustments to my plans as I gained a more in-depth understanding of the project. Timezones gave me trouble as I didn't have much experience working with them before this, luckily, Java.time has a lot of helpful functions for making the mess that is timezone management much more simple. I was able to break most of my functions down into really flexible pieces that worked for different situations. For example, the checkBit() function was able to be used for both food restrictions and hour checking, and my header processing functions worked for both of the different .csv headers. More in-depth explanations for my code decisions are in the Reasoning section of this doc. 
</p>
<p>
When I finished solving the problem, I started the process of refining and securing my code. Throughout the process I did light testing and broke my functions as much as I could, but saving the majority of that work until the end helped me understand each part of my project before I try to reconfigure it too much. My last steps were to add and test some exceptions and further test out some of the edge cases for my functions.
</p>

## Reasoning 
#### Scoring:
<p>

`score = (10 * number of matched food categories) - distance from recipient` 

</p>
<p> I prioritized food matched over distance saved because as a company Copia's goal is to reduce food waste, and I think our customers (both the donors and the donation centers) want to see more food moved as opposed to a speedier delivery.
</p>

#### Storing a List of Recipients but not Customers:
<p>
By trimming the Recipients class to only what I needed, I was able to save enough space to feel comfortable storing the given Recipients.csv and any larger Recipient lists I might need. The potential list of recipients also has a much smaller potential maximum as there are only so many places that will take donated food, while the Customer list could (and hopefully will) grow very quickly. So, for scalability, I decided it would be best to store the Recipients and stream the Customers.
</p>

#### Output CSV Format
<p>
My results are formatted (by row) as an integer representing that Customer's index on the Customer.csv file, followed by an integer that represents the best matched Recipient's index on Recipients.csv, then that match's score, then the index and score of the next best match. This pattern repeats until there are no more valid matches for that Customer. I decided to use the corresponding index as a unique identifier for each of the Customers and Recipients because it saves space despite needing more processing to pull useful information out of it. This decision was also inspired by the integers used for the food categories and availability hours which also follow the priority of space saving. Using the index also meant that illustrating a Customer with no matches was simple, just have a row with a Customer index and no following Recipients.
</p>
<p>
The score is included on the csv file instead of just ranking the Customers because it could be useful for determining other options for drivers, or for use gathering statistics about donation centers. 
</p>

#### `reqRecipHeaderIndices` and `reqCustHeaderIndices`
<p>
These store the corresponding indices of columns on the csv files that are required to complete this assignment. At the expense of some readability, this allows the csv files to have any number of different headers and header orders without changing the functionality of the program as long as all of the required headers are included. I think this would be valuable for scaling as is allows the trimming of the csv files by removing unnecessary information without any changes to the code of the program.
</p>

#### `NUMBER_OF_FOOD_CATEGORIES`
<p>
If more food categories are added, simply changing the value of this constant allows more bits to be checked when comparing matches.
</p>

## Results
<p>
Results are stored in MatchScores.csv, their format and scoring method is described in the Reasoning section above. 
</p>

- Total food categories needing pickup: 578
- Total food categories accepted by best match: 577
- Percentage of food accepted: 99.8%
- Percentage of customers with a matching donation center: 100.0%
- Customers with no matches: 3 (all had a 0 in the food category, so no food to match)
- Maximum number of matches for one customer: 88
- Average number of matches for a customer: 51.06
- Maximum Score: 59.51051
- Average Score: 12.961
- Average best match Score: 28.620
- Average distance from match = 5.62 miles
- Average runtime: 1095.4ms


*note: I don't count the 3 customers with no match in the percentage of matching donation center statistic because they didn't have any food categories that needed pickup*

## Conclusion
In conclusion, I had a good time working on this assignment. I spent more time on it than I though I would (especially this doc), but I learned a lot about working with Kotlin and about the kind of work you guys do on a daily basis. I feel pretty good about the end result and I hope you guys think it looks good too!

##### Thanks for taking the time to read this!
