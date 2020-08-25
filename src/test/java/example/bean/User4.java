package example.bean;

import org.apex.annotation.Value;

/**
 * @author WangYi
 * @since 2020/8/25
 */
public class User4 {
  @Value("${config.test.age}")
  private String aa;

  @Override
  public String toString() {
    return "User4{" +
            "aa='" + aa + '\'' +
            '}';
  }
}
