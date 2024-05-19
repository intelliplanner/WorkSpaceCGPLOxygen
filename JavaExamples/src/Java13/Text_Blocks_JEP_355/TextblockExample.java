package Java13.Text_Blocks_JEP_355;

public class TextblockExample  
{  
    /* Driver Code */  
    @SuppressWarnings("preview")  
    public static void main(String ar[])   
    {  
        String stringtextBlock = """  
                Hi  
                Hello  
                Yes""";  
        String stringLiteral = "Hi\nHello\nYes";  
        System.out.println("Text Block String:\n" + stringtextBlock);  
        System.out.println("Normal String Literal:\n" + stringLiteral);  
    }  
}  