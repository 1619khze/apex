package example.service;

import org.apex.annotation.Singleton;

/**
 * @author WangYi
 * @since 2020/10/29
 */
@Singleton
public class UserServiceImpl implements UserService {

  @Override
  public void say() {
    System.out.println(this.getClass().getName());
  }
}
