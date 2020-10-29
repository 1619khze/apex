package example.service;

/**
 * @author WangYi
 * @since 2020/10/29
 */
public class UserServiceImpl implements UserService {

  @Override
  public void say() {
    System.out.println(this.getClass().getName());
  }
}
