# COVID-19 Correlation Coefficient Calculator

This command line application calculates the Pearson's product-moment correlation coefficients (abbr. coefficient) between the percentage of people that died and got vaccinated of COVID-19 given
- a continent
- or all available countries

Using the [M-Media-Group API](https://github.com/M-Media-Group/Covid-19-API).

## Command line usage

If no arguments given then the coefficient for all counties will be calculated and printed out.

Otherwise - as recognized by the API itself - any:
- input starting with `@` then any string right after is considered a continent.
- 2 letter long string is understood as ISO country code, otherwise country name.

Examples:

- `com.accenture.covid19.App` all countries will be used to get the number.
- `com.accenture.covid19.App HU @Asia` Will calculate the coefficient of Asia continent plus Hungary country.
