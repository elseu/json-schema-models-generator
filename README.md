# JSON-schema models generator (json-schema-models-generator)

> Parses JSON schemas and translates them into Scala case-class models and/or Sangria GraphQL models

This application is a JSON-schema parser and generator for Scala case-class models and (soon) Sangria GraphQL models. It
is currently under development and is currently not guaranteed to work with any JSON schemas apart from the ones tested.

## Table of Contents
- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Maintainers](#maintainers)
- [Acknowledgements](#thanks)
- [Contributing](#contributing)
- [License](#license)

## Background

Sdu works with JSON schemas and often has to work with representations of JSON structures (that obey a particular
schema) in Scala code. This applications allows one to easily generate the required case-class or Sangria model to be
used in Scala applications.

## Install

Download the zip file or clone the repository from [GitHub](https://github.com/elseu/json-schema-models-generator).
 
## Usage

Alter `src/main/scala/nl/sdu/modelsgenerator/builder/ScalaBuilder.scala` to suit your needs and execute:

```sbt run```

## Maintainers

- [Bart Schuller (Lefebvre Sarrut/Sdu Uitgevers)](https://github.com/bartschuller)
- [Francisco Canedo (independent contractor)](https://github.com/fcanedo)

## Acknowledgements

Work performed (under contract) for/by [Lefebvre Sarrut](https://www.lefebvre-sarrut.eu/) / [Sdu Uitgevers](https://www.sdu.nl/).

## Contributing

PRs accepted at the discretion of the maintainers.

## License

Apache License 2.0
