# ❔About

This repo has been created to generate ready to use data files around the Collatz Conjecture. As I
wanted to learn more about these patterns, I needed to generate some custom files.

Finally I quickly developed a first draft, a generator that makes the job at least for my first experimentations.

# 👉Usage

To play with these files, you have two options :

- use ready to use samples
- generate your owns

## 📂Ready to use samples

Ready to use samples are stored in `samples` directory. The directorories are organized
as follow : `${lower-bound}_${upper_bound}`.

So, the `-1000_1000` directory contains sample where seeds are between `-1000` and `1000`.

## 🚀Generate your own samples

The code is organized as a JBang script, so all you have to do to install :

### 💣Install JBang

Linux :

```
sdk install jbang
choco install jbang
```

Windows :

```
choco install jbang
```
### 🕹️Generate your own samples

Shortest way to get done :

```
jbang run --java 11 https://github.com/adriens/JCollatz/blob/main/Collatz.java  -l -666 -u 666
```

Full checkout way :

```shell
git clone https://github.com/adriens/JCollatz.git
cd JCollatz
# Use -666 as lower seed and 666 as upper seed
jbang Collatz.java -l -666 -u 666
ls -la *.csv
ls -la *.graphml
```

Enjoy the following output files :

- `NODES.csv`
- `RELATIONS.csv`
- `Threr_plus_one.graphml`
- The `dot` file

## Playing with `graphviz`

See [official documentation](https://graphviz.org/doc/info/command.html) for more examples
and play with [layouts](https://graphviz.org/docs/layouts/).

Still here is the dumbest graphviz command you can play with :


```
dot -Tpdf collatz_-666_666.graphml.gv -o demo.pdf
```
