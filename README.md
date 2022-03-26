# COVID-19 Correlation Coefficient Calculator

This command line application calculates the Pearson's product-moment correlation coefficients (abbr. coefficient) between the percentage of people that died and got vaccinated of COVID-19 given
- a continent
- or all available countries

Using the [M-Media-Group API](https://github.com/M-Media-Group/Covid-19-API).

## Command line usage

If no arguments given then the tool reads the standard input, where each line is a command. Or else if there is one argument at least then all of them is understood as a command, or if it is "-" then reads the standard input from that argument onwards.

The command can be:
- A continent name (like `Asia`) or empty string (``) to print for all countries.
- Or a country if the argument starts with `@` (Like two letter ISO `@FR` or name as by defined by the API like `@France`)
- `All` in which case it changes from where the numbers are extracted from (By default for all countries/continent it is the field "All", for countries all the individual countries.
- `Safe` if the tool is meant to be data level fail safe or not. This means that the correlation must be forced to use only data which is available both for the `/cases` amnd the `/vaccines` endpoints. Default is: true.

Examples:

- `com.accenture.covid19.App` The command line will be read.
- `com.accenture.covid19.App "" Asia` Will calculate the coefficient of all countries, and Asia continent.

## Extra information

Maybe my questions confused you guys about All or Individual countries. I left the per-country level code there, but that will likely fail for reasons I explained in my last email. The code satisfied the Continent/All requirement, no country level calculation was actually requested originally.

Another issue can be that some countries do not report population, as agreed the percentage must be calculated based on that data, thus if it is not present it can lead to failures.
In "Safe" mode (default) they will be skipped from the calculation.

I believe after all I had to use only the "All" node, and I set this behavior as the default. But in case you want to use the data in individual countries level (Skipping the "All") then you will have `NaN` results. To activate this mode use the `All` command.
