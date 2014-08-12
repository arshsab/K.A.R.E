# K.A.R.E
The Kick-Ass Recommendation Engine for Github.

## How it works

The enine looks at what users have starred and watched to determine relationships between repositories. Relationships are based off of looking at the expectation vs reality of users watching / starring a repository. Specifically, the sorted sequence of repositories by the stars or watchers is generated including all watchers / stars on github from greatest to least. Than a sorted sequence of repositories by the stars or watchers is generated including only the watchers that starred the specified repository from greatest to least. Than, the two sequences are compared; repositories that occur earlier on the second sequence as compared to the first are recommended. For the code please see analysis.

