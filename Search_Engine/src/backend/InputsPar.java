package backend;

import java.util.Scanner;
import java.lang.String;


public class InputsPar {
    private  String[] inputs ;
    private String operator;
    private boolean conjunctive;
    public void setInputs() {
        String[] input  = new String[3];
        Boolean queryCorrect = false;
        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);

        // term which we looking for
        // TODO we have to add  exception error
        System.out.print("searching for: ");
        input[0] = scanner.nextLine();

        // get the type of search
        while (!queryCorrect) {
            System.out.print("Enter your query preferences either C for Conjunctive or D for Disjunctive: ");
            input[1] = scanner.nextLine();
            try {
                if (input[1].equals("c") || input[1].equals("D")) {
                    conjunctive = true;
                    queryCorrect = true;
                } else if (input[1].equals("d") || input[1].equals("D")) {
                    conjunctive = false;
                    queryCorrect = true;
                } else {
                    System.out.println("invalid entry");
                    queryCorrect = false;
                }

            } catch (java.util.InputMismatchException e) {
                System.out.println("invalid entry");
                queryCorrect = false;
            }
        }
        Integer limits = 0;
        try {
            System.out.print("Enter the number of best results: ");
            limits = scanner.nextInt();
            input[2] =  Integer.toString(limits);
        } catch (java.util.InputMismatchException e) {
            System.out.println("invalid entry");
        }
        this.inputs = input;
    }

    public String[] getInputs() {
        return this.inputs;
    }


// stemming the words
    public String[] term(){
        StringBuilder stemBuilder = new StringBuilder();
        String[] words = getInputs()[0].split("\\s+");
        for (String s : words) {
            Stemmer stemmer = new Stemmer();
            char[] chars = s.toCharArray();
            stemmer.add(chars,s.length());
            stemmer.stem();
            stemBuilder.append(stemmer.toString());
            stemBuilder.append(" ");
        }
        String stemmedOutput=stemBuilder.toString();
        String[] term;
        term=  stemmedOutput.split(" ");
        return term;
    }

    public String setPreparedStatement(){
        String whereStatement = "term = ?  ";
        //The And-statement cannot be implemented like this
        for (int i = 1; i < term().length; i++) {
            whereStatement = whereStatement +   " or term = ? ";
        }
        if (conjunctive ){
            operator = "having count (features.docid)=" + Integer.toString(term().length);
        }
        else operator = " ";

        String statement ="select a.docid, a.totalScore ,rank() over (order by totalScore desc) as docrank,url from \n" +
                "(SELECT  features.docid, sum(score)as totalScore,documents.url  from features left join documents on documents.docid= features.docid \n" +
                "where \n" +
                whereStatement +
                "group by features.docid,documents.url \n" +
                operator+
                ") as a \n" +
                "order by docrank  limit \n" +
                inputs[2];

        System.out.println(statement);
        return statement;
    }

}
