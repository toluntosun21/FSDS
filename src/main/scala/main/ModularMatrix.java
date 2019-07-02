package main;
import scala.tools.cmd.gen.AnyVals;

import java.math.BigInteger;
import java.util.Random;

/*
CREATED BY TOLUN TOSUN ON 23.05.2019

IS ABLE TO DO:
    Get modular inverse modulo p, implements Gaussian Elimination
    Get transpose
    Multiply,Add modulo p

 */
public class ModularMatrix {

    private BigInteger[][] data;
    public static BigInteger mod;
    private int rownum;
    private int colnum;

    public ModularMatrix(BigInteger[][] in){
        rownum=in.length;
        colnum=in[0].length;
        data=new BigInteger[rownum][colnum];
        for(int i=0;i<rownum;i++)
            for(int j=0;j<colnum;j++)
                data[i][j]=in[i][j];

    }

    public ModularMatrix(BigInteger[][] in,BigInteger mod_in){
        this(in);
        mod=(mod_in);
    }

    public ModularMatrix(int[][] in){
        rownum=in.length;
        colnum=in[0].length;
        data=new BigInteger[rownum][colnum];
        for(int i=0;i<rownum;i++)
            for(int j=0;j<colnum;j++)
                data[i][j]=BigInteger.valueOf(in[i][j]);
    }

    public ModularMatrix(int[][] in,BigInteger mod_in){
        this(in);
        mod=mod_in;
    }

    public ModularMatrix(int[][] in,int mod){
        this(in,BigInteger.valueOf(mod));
    }

    public ModularMatrix(int row,int col){
        rownum=row;
        colnum=col;
        data=new BigInteger[rownum][colnum];
        for(int i=0;i<rownum;i++)
            for(int j=0;j<colnum;j++)
                data[i][j]=BigInteger.ZERO;
    }

    public BigInteger get(int i,int j){
        return data[i][j];
    }

    public void set(int i,int j,BigInteger num){
        if(i<rownum && j< colnum)
            data[i][j]=num;
    }

    public int getColnum() {
        return colnum;
    }

    public int getRownum() {
        return rownum;
    }

    public ModularMatrix Multiply(ModularMatrix rhs) throws Exception{
        if(this.colnum!=rhs.rownum)
            throw new Exception("matrix sizes are incompatible: "+this.colnum+":"+rhs.rownum);
        if(this.mod!=rhs.mod)
            throw new Exception("matrices from different fields can't be multiplied: "+this.mod+":"+rhs.mod);
        BigInteger[][] out_data=new BigInteger[this.rownum][rhs.colnum];
        for(int i=0;i<this.rownum;i++)
            for(int j=0;j<rhs.colnum;j++) {
                BigInteger sum= BigInteger.ZERO;
                for (int k = 0; k < this.colnum; k++) {
                    sum=sum.add(this.data[i][k].multiply(rhs.data[k][j]));
                }
                sum=sum.mod(mod);
                out_data[i][j]=sum;
            }
        return new ModularMatrix(out_data,this.mod);
    }

    public ModularMatrix Add(ModularMatrix rhs) throws Exception{
        if(this.rownum!=rhs.rownum || this.colnum!=rhs.colnum)
            throw new Exception("Matrix sizes are inequal: "+this.rownum+"x"+this.colnum+" vs. "+rhs.rownum+"x"+rhs.colnum);

        BigInteger[][] out_data=new BigInteger[colnum][rownum];
        for(int i=0;i<rownum;i++)for(int j=0;j<colnum;j++)
            out_data[i][j]=this.data[i][j].add(rhs.data[i][j]).mod(mod);
        return new ModularMatrix(out_data,mod);

    }

    public BigInteger DotProduct(ModularMatrix rhs) throws Exception{
        if(this.rownum==1)
        return this.Multiply(rhs.Transpose()).data[0][0];
        else if(rhs.rownum==1)return rhs.Multiply(this.Transpose()).data[0][0];
        else throw new Exception("Dot product not applicable");
    }

    public ModularMatrix Transpose(){
        BigInteger[][] out_data=new BigInteger[this.colnum][this.rownum];
        for(int i=0;i<rownum;i++)
            for(int j=0;j<colnum;j++)
                out_data[j][i]=data[i][j];
        return new ModularMatrix(out_data,mod);
    }


    /*
    Helper functions for Gaussian Elimination
     */
    private BigInteger[] MultiplyRow(BigInteger[] in,BigInteger coeff){
        BigInteger[] out=new BigInteger[in.length];
        for(int i=0;i<in.length;i++)
            out[i]=in[i].multiply(coeff).mod(mod);
        return out;
    }

    private BigInteger[] SubtractRow(BigInteger[] lhs,BigInteger[] rhs){
        BigInteger[] out=new BigInteger[lhs.length];
        for(int i=0;i<lhs.length;i++)
            out[i]=lhs[i].subtract(rhs[i]).mod(mod);
        return out;
    }

    private void MultiplyAndSubtract(ModularMatrix mat,BigInteger multiplier,int goal_row,int multiplied_row){
        BigInteger[] multiplied = MultiplyRow(mat.data[multiplied_row], multiplier);
        BigInteger[] subtracted =
                SubtractRow(mat.data[goal_row], multiplied);
        mat.data[goal_row] = subtracted;
    }

    private void InterchangeRows(ModularMatrix mat,int i,int j){
        BigInteger[] temp=mat.data[i];
        mat.data[i]=mat.data[j];
        mat.data[j]=temp;
    }

    public ModularMatrix Inverse() throws Exception{
        try{
            return InverseStrategy();
        }catch(Exception e){
            if(e.getMessage().contains("matrix"))
                return InverseStrategy();
            else throw e;
        }
    }

    /*
    implements Gaussian Elimination
     */
    public ModularMatrix InverseStrategy() throws Exception{
        if(rownum!=colnum)
            throw new Exception("non-square matrix is non-invertible: "+rownum+"x"+colnum);
        ModularMatrix augmented_2=UnitMatrix(rownum,mod);
        ModularMatrix augmented_1=new ModularMatrix(data,mod);

        Random ran=new Random();
        for(int i=0;i<rownum*2;i++){
            int num1=ran.nextInt(rownum);
            int num2=ran.nextInt(rownum);
            if(num1!=num2){
                InterchangeRows(augmented_1,num1,num2);
                InterchangeRows(augmented_2,num1,num2);
            }
                else i--;
        }

/*        System.out.println("init");
        System.out.println(augmented_1);*/
        /*
        Preprocess until no diagonal zero exists in the system
         */
        for(int i=0;i<rownum;i++){
            if(augmented_1.data[i][i].equals(BigInteger.ZERO)){
                boolean done=false;
                for(int j=0;j<rownum;j++){
                    if(augmented_1.data[j][j].equals(BigInteger.ZERO)==false){
                        InterchangeRows(augmented_1,i,j);
                        InterchangeRows(augmented_2,i,j);
                        done=true;
                        break;
                    }
                }
                if(!done)throw new Exception("Non-Invertible(Singular) matrix");
            }
        }
/*        System.out.println("after preprocess");
        System.out.println(augmented_1);*/

        for(int i=0;i<rownum-1;i++){//i^th iteration set i^th columns 0
            for(int j=0;j<rownum-1-i;j++){
                int row_bottom=rownum-1-j;
                int row_top=row_bottom-1;//init
//                System.out.println(i+":"+j);

                while(row_top>=i && (augmented_1.data[row_top][i].equals(BigInteger.ZERO)==false ||
                        augmented_1.data[row_top][row_bottom].equals(BigInteger.ZERO))){
                    row_top--;
                }
                if(row_top>=i){//we are finished
                    InterchangeRows(augmented_1,row_top,row_bottom);
                    InterchangeRows(augmented_2,row_top,row_bottom);
                }else{//we are not finished
                    row_top=row_bottom-1;
                    BigInteger multiplier = augmented_1.data[row_top][i].modInverse(mod);
                    multiplier = multiplier.multiply(augmented_1.data[row_bottom][i]).mod(mod);
                    while((augmented_1.data[row_top][row_bottom].multiply(multiplier).mod(mod).equals(
                            augmented_1.data[row_bottom][row_bottom]))
                    ){

                        row_top--;
                        if(row_top<i)break;
                        multiplier = augmented_1.data[row_top][i].modInverse(mod);
                        multiplier = multiplier.multiply(augmented_1.data[row_bottom][i]).mod(mod);
                    }
                    if(row_top>=i){
                        MultiplyAndSubtract(augmented_1, multiplier, row_bottom, row_top);
                        MultiplyAndSubtract(augmented_2, multiplier, row_bottom, row_top);
                    }else{
                        throw new Exception(("Non-Invertible(Singular) matrix"));
                    }
                }

  //              System.out.println(augmented_1);
            }
        }
  /*      System.out.println("step1");
        System.out.println(augmented_1);*/

        /*
        At this point, we have a lower triangle filled with 0s
         */
        for(int i=0;i<rownum-1;i++){
            //i^th iteration sets rownum-1-i^th columns 0
            //i^th row is used to set i^th column 0
            int row_to_multiply=rownum-1-i;
            int col_id=row_to_multiply;

            for(int j=0;j<rownum-1-i;j++){
                //j^th row is processed
                BigInteger multiplier=augmented_1.data[row_to_multiply][col_id].modInverse(mod);
                multiplier= multiplier.
                        multiply(augmented_1.data[j][col_id]).mod(mod);
                if(multiplier.equals(BigInteger.ZERO)==false) {
                    MultiplyAndSubtract(augmented_1, multiplier, j, row_to_multiply);
                    MultiplyAndSubtract(augmented_2, multiplier, j, row_to_multiply);
                }
            }
        }
/*        System.out.println("step2");
        System.out.println(augmented_1);*/

        /*
        At this point, we have a diagonal matrix, it is time to set all the diagonal entries to 1
         */
        for(int i=0;i<rownum;i++){
            if(augmented_1.data[i][i].equals(BigInteger.ZERO))
                throw new Exception("Non-Invertible(Singular) matrix");
            BigInteger inv=augmented_1.data[i][i].modInverse(mod);
            augmented_1.data[i]=MultiplyRow(augmented_1.data[i],inv);
            augmented_2.data[i]=MultiplyRow(augmented_2.data[i],inv);
        }

        return augmented_2;
    }

    @Override
    public String toString() {
        String s="";
        for(int i=0;i<rownum;i++) {
            for (int j = 0; j < colnum; j++) {
                s += data[i][j].toString();
                if (j != colnum - 1)
                    s += "\t";
            }
            if(i!=rownum-1)s+="\n";
        }
        return s;
    }

    public static ModularMatrix UnitMatrix(int size,BigInteger mod){
        int init[][]=new int[size][size];
        for(int i=0;i<size;i++){
            init[i][i]=1;
        }
        return new ModularMatrix(init,mod);
    }

    public static ModularMatrix UnitMatrix(int size,int mod){
        return UnitMatrix(size,BigInteger.valueOf(mod));
    }


}
